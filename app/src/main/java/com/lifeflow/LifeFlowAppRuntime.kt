package com.lifeflow

import android.content.Context
import android.util.Log
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class LifeFlowAppRuntime(
    private val applicationContext: Context
) : StartupRuntimeEntryPoint, AutoCloseable {

    companion object {
        private const val TAG = "LifeFlowAppRuntime"
    }

    @Volatile
    private var runtimeBindings: LifeFlowAppRuntimeBindings? = null

    @Volatile
    private var startupInitialized = false

    @Volatile
    private var startupFailureMessage: String? = null

    val hardeningReport: SecurityHardeningGuard.HardeningReport?
        get() = runtimeBindings?.hardeningReport

    private val startupInitLock = Any()

    override fun ensureStarted(): Boolean {
        if (startupInitialized) return true
        synchronized(startupInitLock) {
            if (startupInitialized) return true
            return try {
                runtimeBindings = LifeFlowAppGraphFactory.createRuntimeBindings(
                    applicationContext = applicationContext,
                    isInstrumentation = isRunningInstrumentation()
                )
                startupInitialized = true
                startupFailureMessage = null
                true
            } catch (t: Throwable) {
                runtimeBindings?.close()
                runtimeBindings = null
                startupInitialized = false
                startupFailureMessage = buildStartupFailureMessage(t)
                Log.e(TAG, "Startup initialization failed.", t)
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

    override fun readStartupFailureMessage(): String? {
        return startupFailureMessage
    }

    override fun close() {
        synchronized(startupInitLock) {
            runtimeBindings?.close()
            runtimeBindings = null
            startupInitialized = false
            startupFailureMessage = null
        }
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
}
