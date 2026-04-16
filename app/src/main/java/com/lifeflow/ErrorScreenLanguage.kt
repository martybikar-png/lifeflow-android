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
            guidanceTitle = "A calmer recovery step is needed",
            guidanceMessage = "LifeFlow paused protected access because this state needs a recovery step before sensitive features can continue.",
            nextStepMessage = "Reset the vault to create a fresh protected baseline, then return to protected features.",
            buttonLabel = "Reset vault"
        )

        isSecurityDegradedMessage(message) -> ErrorScreenContent(
            guidanceTitle = "A safer state check is needed",
            guidanceMessage = "LifeFlow is keeping protected access limited until the current state is refreshed.",
            nextStepMessage = "Authenticate again to restore a fresh protected state before continuing.",
            buttonLabel = "Authenticate again"
        )

        isSessionExpiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Protected access needs a fresh session",
            guidanceMessage = "Your earlier protected session is no longer active, so sensitive surfaces stay paused for now.",
            nextStepMessage = "Authenticate again to continue with a fresh protected session.",
            buttonLabel = "Authenticate again"
        )

        isAuthenticationRequiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Authentication is needed to continue",
            guidanceMessage = "LifeFlow needs a fresh authentication step before protected access can continue.",
            nextStepMessage = "Authenticate again when you are ready to continue safely.",
            buttonLabel = "Authenticate again"
        )

        else -> ErrorScreenContent(
            guidanceTitle = "Protected access is paused for now",
            guidanceMessage = "LifeFlow is keeping sensitive surfaces paused until the current state is safely resolved.",
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
