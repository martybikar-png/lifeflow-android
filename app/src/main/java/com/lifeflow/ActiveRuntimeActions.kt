package com.lifeflow

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient
import com.lifeflow.security.BiometricAuthManager
import com.lifeflow.security.SecurityVaultResetAuthorization

internal fun requestActiveRuntimeRefreshWithUiFeedback(
    viewModel: ActiveRuntimeViewModelContract,
    requestMessage: String,
    setLastAction: (String) -> Unit
) {
    try {
        viewModel.refreshMetricsAndTwinNow()
        setLastAction(requestMessage)
    } catch (exception: Exception) {
        setLastAction(
            "Refresh trigger failed: ${exception::class.java.simpleName}: ${exception.message}"
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

    try {
        onStartIntent(hcSettingsIntent)
        onSettingsOpened()
        setLastAction("Opened Health Connect settings")
    } catch (primaryError: Exception) {
        try {
            onStartIntent(appSettingsIntent)
            onSettingsOpened()
            setLastAction(
                "HC settings unavailable (${primaryError::class.java.simpleName}). Opened App settings instead."
            )
        } catch (fallbackError: Exception) {
            onSettingsOpenFailed()
            setLastAction(
                "Unable to open settings: ${primaryError::class.java.simpleName} / ${fallbackError::class.java.simpleName}"
            )
        }
    }
}

internal fun requestActiveRuntimeBiometricAuthentication(
    biometricAuthManager: BiometricAuthManager,
    viewModel: ActiveRuntimeViewModelContract,
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

internal fun requestActiveRuntimeVaultResetAuthentication(
    biometricAuthManager: BiometricAuthManager,
    viewModel: ActiveRuntimeViewModelContract,
    setLastAction: (String) -> Unit
) {
    setLastAction("Vault reset authentication requested")

    if (biometricAuthManager.hasAuthPerUseCrypto()) {
        biometricAuthManager.authenticateForVaultResetAuthPerUseCrypto(
            onSuccess = {
                completeActiveRuntimeVaultResetAuthorization(
                    grantAuthorization = {
                        SecurityVaultResetAuthorization.grantFromVaultResetAuthPerUseSuccess()
                    },
                    successMessage = "Vault reset auth-per-use authentication succeeded",
                    viewModel = viewModel,
                    setLastAction = setLastAction
                )
            },
            onError = { msg ->
                SecurityVaultResetAuthorization.clear()
                val resolvedMessage = msg.ifBlank { "Unknown biometric error" }
                setLastAction("Vault reset authentication failed: $resolvedMessage")
                viewModel.onAuthenticationError(resolvedMessage)
            }
        )
        return
    }

    biometricAuthManager.authenticateForVaultReset(
        onSuccess = {
            completeActiveRuntimeVaultResetAuthorization(
                grantAuthorization = {
                    SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
                },
                successMessage = "Vault reset biometric authentication succeeded",
                viewModel = viewModel,
                setLastAction = setLastAction
            )
        },
        onError = { msg ->
            SecurityVaultResetAuthorization.clear()
            val resolvedMessage = msg.ifBlank { "Unknown biometric error" }
            setLastAction("Vault reset authentication failed: $resolvedMessage")
            viewModel.onAuthenticationError(resolvedMessage)
        }
    )
}

internal fun completeActiveRuntimeVaultResetAuthorization(
    grantAuthorization: () -> Unit,
    successMessage: String,
    viewModel: ActiveRuntimeViewModelContract,
    setLastAction: (String) -> Unit
) {
    try {
        grantAuthorization()
        setLastAction(successMessage)
        viewModel.resetVault()
    } catch (exception: Exception) {
        SecurityVaultResetAuthorization.clear()
        val resolvedMessage = exception.message?.takeIf { it.isNotBlank() }
            ?: "Vault reset authorization failed"
        setLastAction("Vault reset authentication failed: $resolvedMessage")
        viewModel.onAuthenticationError(resolvedMessage)
    }
}
