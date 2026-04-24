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
            guidanceTitle = "Recovery needed",
            guidanceMessage = "Protected access is paused.",
            nextStepMessage = "Reset the vault to create a fresh baseline.",
            buttonLabel = "Reset vault"
        )

        isSecurityDegradedMessage(message) -> ErrorScreenContent(
            guidanceTitle = "State check needed",
            guidanceMessage = "Protected access is limited for now.",
            nextStepMessage = "Authenticate again to refresh trust.",
            buttonLabel = "Authenticate again"
        )

        isSessionExpiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Session expired",
            guidanceMessage = "Protected access needs a fresh session.",
            nextStepMessage = "Authenticate again to continue.",
            buttonLabel = "Authenticate again"
        )

        isAuthenticationRequiredMessage(message) -> ErrorScreenContent(
            guidanceTitle = "Authentication needed",
            guidanceMessage = "Protected access is waiting for you.",
            nextStepMessage = "Authenticate again when ready.",
            buttonLabel = "Authenticate again"
        )

        else -> ErrorScreenContent(
            guidanceTitle = "Access paused",
            guidanceMessage = "LifeFlow is holding protected access.",
            nextStepMessage = "Authenticate again to continue.",
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