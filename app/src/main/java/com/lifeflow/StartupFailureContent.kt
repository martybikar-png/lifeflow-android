package com.lifeflow

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun StartupFailureContent(
    initialStartupFailureMessage: String,
    retryStartup: () -> Boolean,
    readStartupFailureMessage: () -> String,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit,
    onRecreateActivity: () -> Unit
) {
    var startupFailureMessage by remember {
        mutableStateOf(initialStartupFailureMessage)
    }
    var lastAction by remember { mutableStateOf("Startup paused.") }
    var pendingSettingsRetry by remember { mutableStateOf(false) }

    fun retryStartupWithMessage(requestMessage: String) {
        lastAction = requestMessage

        val initialized = retryStartup()

        if (initialized) {
            lastAction = "Startup recovered."
            onRecreateActivity()
        } else {
            startupFailureMessage = readStartupFailureMessage()
            lastAction = "Startup still paused."
        }
    }

    fun openAppSettings() {
        runCatching {
            onStartIntent(buildAppSettingsIntent(appPackageName))
        }.onSuccess {
            pendingSettingsRetry = true
            lastAction = "Settings opened."
        }.onFailure {
            pendingSettingsRetry = false
            lastAction = "Settings could not open."
        }
    }

    HandleStartupFailurePendingResumeAction(
        pending = pendingSettingsRetry,
        onConsumePending = { pendingSettingsRetry = false },
        onResumeAction = {
            retryStartupWithMessage("Checking startup again.")
        }
    )

    StartupFailureScreen(
        message = startupFailureMessage,
        lastAction = lastAction,
        onRetryStartup = {
            retryStartupWithMessage("Retrying startup.")
        },
        onOpenAppSettings = {
            openAppSettings()
        }
    )
}