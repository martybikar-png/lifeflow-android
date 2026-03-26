package com.lifeflow

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient
import com.lifeflow.security.BiometricAuthManager

internal fun requestActiveRuntimeRefreshWithUiFeedback(
    viewModel: MainViewModel,
    requestMessage: String,
    setLastAction: (String) -> Unit
) {
    runCatching {
        viewModel.refreshMetricsAndTwinNow()
    }.onSuccess {
        setLastAction(requestMessage)
    }.onFailure {
        setLastAction(
            "Refresh trigger failed: ${it::class.java.simpleName}: ${it.message}"
        )
    }
}

internal fun openActiveRuntimeHealthConnectSettingsWithFallback(
    appPackageName: String,
    onStartIntent: (Intent) -> Unit,
    onSettingsOpened: () -> Unit,
    onSettingsOpenFailed: () -> Unit,
    setLastAction: (String) -> Unit
) {
    val hcSettingsIntent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", appPackageName, null)
    }

    runCatching {
        onStartIntent(hcSettingsIntent)
    }.onSuccess {
        onSettingsOpened()
        setLastAction("Opened Health Connect settings")
    }.onFailure { primaryError ->
        runCatching {
            onStartIntent(appSettingsIntent)
        }.onSuccess {
            onSettingsOpened()
            setLastAction(
                "HC settings unavailable (${primaryError::class.java.simpleName}). Opened App settings instead."
            )
        }.onFailure { fallbackError ->
            onSettingsOpenFailed()
            setLastAction(
                "Unable to open settings: ${primaryError::class.java.simpleName} / ${fallbackError::class.java.simpleName}"
            )
        }
    }
}

internal fun requestActiveRuntimeBiometricAuthentication(
    biometricAuthManager: BiometricAuthManager,
    viewModel: MainViewModel,
    setLastAction: (String) -> Unit
) {
    setLastAction("Biometric authentication requested")
    biometricAuthManager.authenticate(
        onSuccess = {
            setLastAction("Biometric authentication succeeded")
            viewModel.onAuthenticationSuccess()
        },
        onError = { msg ->
            val resolvedMessage = msg.ifBlank { "Unknown biometric error" }
            setLastAction("Biometric authentication failed: $resolvedMessage")
            viewModel.onAuthenticationError(resolvedMessage)
        }
    )
}
