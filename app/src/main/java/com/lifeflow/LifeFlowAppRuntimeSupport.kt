package com.lifeflow

import android.content.Context
import android.util.Log
import java.util.UUID

internal const val LIFEFLOW_APP_RUNTIME_TAG = "LifeFlowAppRuntime"
private const val STARTUP_PHASE_APPLICATION_STARTUP = "application_startup"
private const val STARTUP_SCHEMA_VERSION = 1

internal fun buildLifeFlowAppRuntimeStartupIntegrityRequestContext(
    context: Context
): IntegrityStartupRequestContext {
    val requestedAtEpochMs = System.currentTimeMillis()
    val startupRequestId = UUID.randomUUID().toString()

    return IntegrityStartupRequestContext(
        schemaVersion = STARTUP_SCHEMA_VERSION,
        packageName = context.packageName,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        buildType = if (BuildConfig.DEBUG) "debug" else "release",
        startupPhase = STARTUP_PHASE_APPLICATION_STARTUP,
        startupTrigger = IntegrityStartupCheckTrigger.APPLICATION_COLD_START.payloadValue,
        startupProcessSequence = 1,
        startupRequestId = startupRequestId,
        requestedAtEpochMs = requestedAtEpochMs
    )
}

internal fun buildLifeFlowAppRuntimeStartupFailureMessage(
    throwable: Throwable
): String {
    val type = throwable::class.java.simpleName.ifBlank { "UnknownError" }
    val detail = throwable.message?.takeIf { it.isNotBlank() } ?: "unknown"
    return "Application startup failed: $type: $detail"
}

internal fun isLifeFlowAppRuntimeRunningInstrumentation(): Boolean {
    return try {
        Class.forName("androidx.test.platform.app.InstrumentationRegistry")
        true
    } catch (_: Throwable) {
        false
    }
}

internal fun logLifeFlowAppRuntimeInfo(message: String) {
    runCatching {
        Log.i(LIFEFLOW_APP_RUNTIME_TAG, message)
    }
}

internal fun logLifeFlowAppRuntimeWarning(
    message: String,
    throwable: Throwable? = null
) {
    runCatching {
        if (throwable == null) {
            Log.w(LIFEFLOW_APP_RUNTIME_TAG, message)
        } else {
            Log.w(LIFEFLOW_APP_RUNTIME_TAG, message, throwable)
        }
    }
}

internal fun logLifeFlowAppRuntimeError(
    message: String,
    throwable: Throwable
) {
    runCatching {
        Log.e(LIFEFLOW_APP_RUNTIME_TAG, message, throwable)
    }
}
