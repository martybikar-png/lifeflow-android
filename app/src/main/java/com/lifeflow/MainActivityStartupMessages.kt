package com.lifeflow

internal fun startupStatusLabel(message: String): String {
    return when {
        message.contains("permission", ignoreCase = true) ->
            "Startup blocked by missing or denied permissions."

        message.contains("security", ignoreCase = true) ->
            "Startup blocked by a security-related condition."

        message.contains("initialize", ignoreCase = true) ->
            "Startup did not finish initialization."

        else ->
            "Startup is blocked and requires recovery before the app can continue."
    }
}

internal fun startupRecoveryGuidance(message: String): String {
    return when {
        message.contains("permission", ignoreCase = true) ->
            "Check app permissions in system settings, then retry startup."

        message.contains("security", ignoreCase = true) ->
            "A security-related startup issue was detected. Review access settings, then retry startup."

        message.contains("initialize", ignoreCase = true) ->
            "Startup initialization did not complete. Retry first. If the problem repeats, open App settings and return."

        else ->
            "Retry startup first. If the error continues, open App settings, then return and try again."
    }
}

internal fun startupRecoveryActionHint(message: String): String {
    return when {
        message.contains("permission", ignoreCase = true) ->
            "Permissions-related startup failures usually need App settings before retrying."

        message.contains("security", ignoreCase = true) ->
            "Security-related startup failures usually need settings review before retrying startup."

        message.contains("initialize", ignoreCase = true) ->
            "Initialization failures usually recover with a retry first."

        else ->
            "Retry startup first. If that does not help, open App settings and then return here."
    }
}