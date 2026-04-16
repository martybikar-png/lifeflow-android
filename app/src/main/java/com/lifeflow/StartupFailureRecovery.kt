package com.lifeflow

import android.content.Intent
import android.net.Uri
import android.provider.Settings

private const val UNKNOWN_STARTUP_FAILURE_MESSAGE =
    "Application startup failed for an unknown reason."

internal fun tryEnsureStartupInitialized(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint
): Boolean {
    return runCatching {
        startupRuntimeEntryPoint.ensureStarted()
    }.getOrElse {
        false
    }
}

internal fun readStartupFailureMessage(
    startupRuntimeEntryPoint: StartupRuntimeEntryPoint
): String {
    return startupRuntimeEntryPoint.readStartupFailureMessage()
        ?: UNKNOWN_STARTUP_FAILURE_MESSAGE
}

internal fun buildAppSettingsIntent(appPackageName: String): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", appPackageName, null)
    }
}
