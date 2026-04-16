package com.lifeflow

import android.content.Context
import android.util.Log
import com.lifeflow.security.IntegrityTrustRuntime
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider
import com.lifeflow.security.SecurityIntegrityTrustAuthority
import com.lifeflow.security.SecurityIntegrityTrustVerdict
import com.lifeflow.security.hardening.SecurityHardeningGuard
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

internal class LifeFlowAppRuntime(
    private val applicationContext: Context,
    private val runtimeBindingsFactory: (Context, Boolean) -> LifeFlowAppRuntimeBindings =
        { context, isInstrumentation ->
            LifeFlowAppGraphFactory.createRuntimeBindings(
                applicationContext = context,
                isInstrumentation = isInstrumentation
            )
        },
    private val launchBackgroundTask: (String, () -> Unit) -> Unit = { name, block ->
        thread(
            start = true,
            isDaemon = true,
            name = name
        ) {
            block()
        }
    },
    private val reportIntegrityTrustVerdictResponse: (IntegrityTrustVerdictResponse) -> Unit =
        { response ->
            SecurityIntegrityTrustAuthority.reportVerdictResponse(response)
        },
    private val startupIntegrityContextFactory: (Context) -> IntegrityStartupRequestContext =
        { context ->
            buildStartupIntegrityRequestContext(context)
        }
) : StartupRuntimeEntryPoint, AutoCloseable {

    companion object {
        private const val TAG = "LifeFlowAppRuntime"
        private const val STARTUP_PHASE_APPLICATION_STARTUP = "application_startup"
        private const val STARTUP_SCHEMA_VERSION = 1

        private fun buildStartupIntegrityRequestContext(
            context: Context
        ): IntegrityStartupRequestContext {
            return IntegrityStartupRequestContext(
                schemaVersion = STARTUP_SCHEMA_VERSION,
                packageName = context.packageName,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                buildType = if (BuildConfig.DEBUG) "debug" else "release",
                startupPhase = STARTUP_PHASE_APPLICATION_STARTUP,
                requestedAtEpochMs = System.currentTimeMillis()
            )
        }
    }

    @Volatile
    private var runtimeBindings: LifeFlowAppRuntimeBindings? = null

    @Volatile
    private var startupInitialized = false

    @Volatile
    private var startupFailureMessage: String? = null

    @Volatile
    private var integrityStartupCheckScheduled = false

    val hardeningReport: SecurityHardeningGuard.HardeningReport?
        get() = runtimeBindings?.hardeningReport

    private val startupInitLock = Any()

    override fun ensureStarted(): Boolean {
        if (startupInitialized) return true
        synchronized(startupInitLock) {
            if (startupInitialized) return true
            return try {
                runtimeBindings = runtimeBindingsFactory(
                    applicationContext,
                    isRunningInstrumentation()
                )
                startupInitialized = true
                startupFailureMessage = null
                integrityStartupCheckScheduled = false
                true
            } catch (t: Throwable) {
                runtimeBindings?.close()
                runtimeBindings = null
                startupInitialized = false
                startupFailureMessage = buildStartupFailureMessage(t)
                integrityStartupCheckScheduled = false
                logError("Startup initialization failed.", t)
                false
            }
        }
    }

    override fun requireMainViewModelFactory(): MainViewModelFactory {
        return requireRuntimeBindings().mainViewModelFactory
    }

    override fun authPerUseCryptoProviderOrNull(): SecurityAuthPerUseCryptoProvider? {
        return requireRuntimeBindings().authPerUseCryptoProvider
    }

    override fun requireIntegrityTrustRuntime(): IntegrityTrustRuntime {
        return requireRuntimeBindings().integrityTrustRuntime
    }

    override fun scheduleIntegrityTrustStartupCheck() {
        val integrityTrustRuntime = synchronized(startupInitLock) {
            val currentBindings = runtimeBindings ?: return
            if (!tryScheduleIntegrityTrustStartupCheckLocked(
                    isConfigured = currentBindings.integrityTrustRuntime.isConfigured()
                )
            ) {
                return
            }
            currentBindings.integrityTrustRuntime
        }

        launchBackgroundTask("LifeFlow-IntegrityStartupCheck") {
            runIntegrityTrustStartupCheckNow(
                requestServerVerdict = integrityTrustRuntime::requestServerVerdict
            )
        }
    }

    internal fun tryScheduleIntegrityTrustStartupCheck(
        isConfigured: Boolean
    ): Boolean = synchronized(startupInitLock) {
        tryScheduleIntegrityTrustStartupCheckLocked(isConfigured)
    }

    internal fun runIntegrityTrustStartupCheckNow(
        requestServerVerdict: suspend (String) -> IntegrityTrustVerdictResponse
    ) {
        val requestContext = startupIntegrityContextFactory(applicationContext)
        val payload = requestContext.serializeIntegrityPayload()

        runCatching {
            runBlocking {
                requestServerVerdict(payload)
            }
        }.onSuccess { response ->
            reportIntegrityTrustVerdictResponse(response)
            logInfo(
                "Startup integrity trust verdict applied: ${response.verdict} (${response.reason})"
            )
        }.onFailure { throwable ->
            reportIntegrityTrustVerdictResponse(
                IntegrityTrustVerdictResponse(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = "STARTUP_VERDICT_FAILURE: ${throwable::class.java.simpleName}: ${throwable.message}"
                )
            )
            logWarning(
                "Startup integrity trust verdict request failed.",
                throwable
            )
        }
    }

    override fun readStartupFailureMessage(): String? {
        return startupFailureMessage
    }

    override fun close() {
        synchronized(startupInitLock) {
            runtimeBindings?.close()
            runtimeBindings = null
            startupInitialized = false
            startupFailureMessage = null
            integrityStartupCheckScheduled = false
        }
    }

    private fun tryScheduleIntegrityTrustStartupCheckLocked(
        isConfigured: Boolean
    ): Boolean {
        if (integrityStartupCheckScheduled) {
            return false
        }
        if (!isConfigured) {
            return false
        }

        integrityStartupCheckScheduled = true
        return true
    }

    private fun requireRuntimeBindings(): LifeFlowAppRuntimeBindings {
        return runtimeBindings ?: error("LifeFlowAppRuntimeBindings is not initialized.")
    }

    private fun buildStartupFailureMessage(t: Throwable): String {
        val type = t::class.java.simpleName.ifBlank { "UnknownError" }
        val detail = t.message?.takeIf { it.isNotBlank() } ?: "unknown"
        return "Application startup failed: $type: $detail"
    }

    private fun isRunningInstrumentation(): Boolean {
        return try {
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun logInfo(message: String) {
        runCatching {
            Log.i(TAG, message)
        }
    }

    private fun logWarning(
        message: String,
        throwable: Throwable? = null
    ) {
        runCatching {
            if (throwable == null) {
                Log.w(TAG, message)
            } else {
                Log.w(TAG, message, throwable)
            }
        }
    }

    private fun logError(
        message: String,
        throwable: Throwable
    ) {
        runCatching {
            Log.e(TAG, message, throwable)
        }
    }
}
