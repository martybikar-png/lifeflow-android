package com.lifeflow

internal data class ErrorScreenContent(
    val guidanceTitle: String,
    val guidanceMessage: String,
    val nextStepMessage: String,
    val buttonLabel: String
)

internal fun resolveErrorScreenContent(
    message: String,
    resetRequired: Boolean
): ErrorScreenContent {
    return when {
        resetRequired || isRecoveryRequiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Recovery is required",
            guidanceMessage = "LifeFlow detected a security state that requires recovery before protected access can continue.",
            nextStepMessage = "Reset the vault to establish a fresh protected baseline before returning to sensitive features.",
            buttonLabel = "Reset vault"
        )

        isSecurityDegradedMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Security state is degraded",
            guidanceMessage = "Protected access is intentionally limited because the current trust state is degraded.",
            nextStepMessage = "Authenticate again before returning to sensitive actions or protected data.",
            buttonLabel = "Authenticate again"
        )

        isSessionExpiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Protected session expired",
            guidanceMessage = "Your previous protected session is no longer active, so LifeFlow keeps sensitive surfaces closed.",
            nextStepMessage = "Authenticate again to restore a fresh protected session before continuing.",
            buttonLabel = "Authenticate again"
        )

        isAuthenticationRequiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Authentication is required",
            guidanceMessage = "LifeFlow cannot continue into protected access until authentication is verified again.",
            nextStepMessage = "Authenticate again to continue safely.",
            buttonLabel = "Authenticate again"
        )

        else -> ErrorScreenContent(
            guidanceTitle = "Protected access is blocked",
            guidanceMessage = "LifeFlow is keeping sensitive surfaces closed until the current state is safely resolved.",
            nextStepMessage = "Review the current state and authenticate again when you are ready to continue.",
            buttonLabel = "Authenticate again"
        )
    }
}

internal fun isRecoveryRequiredMessage(message: String): Boolean {
    return message.contains("Reset vault is required", ignoreCase = true) ||
            message.contains("vault reset is required", ignoreCase = true) ||
            message.contains("Security compromised", ignoreCase = true)
}

internal fun isSecurityDegradedMessage(message: String): Boolean {
    return message.contains("Security degraded", ignoreCase = true)
}

internal fun isSessionExpiredMessage(message: String): Boolean {
    return message.contains("Session expired", ignoreCase = true)
}

internal fun isAuthenticationRequiredMessage(message: String): Boolean {
    return message.contains("Authentication required", ignoreCase = true) ||
            message.contains("Active auth session missing", ignoreCase = true) ||
            message.contains("Access locked", ignoreCase = true)
}
