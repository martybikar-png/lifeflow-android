package com.lifeflow

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun StartupFailureContent(
    app: LifeFlowApplication,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit,
    onRecreateActivity: () -> Unit
) {
    var startupFailureMessage by remember {
        mutableStateOf(readStartupFailureMessage(app))
    }
    var lastAction by remember { mutableStateOf("Startup initialization failed") }
    var pendingSettingsRetry by remember { mutableStateOf(false) }

    fun retryStartup(requestMessage: String) {
        lastAction = requestMessage

        val initialized = tryEnsureStartupInitialized(app)

        if (initialized) {
            lastAction = "$requestMessage; startup recovered, recreating activity"
            onRecreateActivity()
        } else {
            startupFailureMessage = readStartupFailureMessage(app)
            lastAction = "$requestMessage; startup still failing"
        }
    }

    fun openAppSettings() {
        runCatching {
            onStartIntent(buildAppSettingsIntent(appPackageName))
        }.onSuccess {
            pendingSettingsRetry = true
            lastAction = "Opened App settings"
        }.onFailure {
            pendingSettingsRetry = false
            lastAction = "Unable to open App settings: ${it::class.java.simpleName}"
        }
    }

    HandleActiveRuntimePendingResumeAction(
        pending = pendingSettingsRetry,
        onConsumePending = { pendingSettingsRetry = false },
        onResumeAction = {
            retryStartup("Returned from settings; startup retry requested")
        }
    )

    StartupFailureScreen(
        message = startupFailureMessage,
        lastAction = lastAction,
        onRetryStartup = {
            retryStartup("Manual startup retry requested")
        },
        onOpenAppSettings = {
            openAppSettings()
        }
    )
}
