package com.lifeflow

internal fun startupStatusLabel(message: String): String {
    return when {
        message.contains("hardening", ignoreCase = true) ||
        message.contains("root", ignoreCase = true) ||
        message.contains("debugger", ignoreCase = true) ->
            "Startup blocked by device safety."
        message.contains("permission", ignoreCase = true) ->
            "Startup needs permissions."
        message.contains("security", ignoreCase = true) ->
            "Startup needs a security check."
        message.contains("initialize", ignoreCase = true) ->
            "Startup did not finish."
        else ->
            "Startup needs recovery."
    }
}

internal fun startupRecoveryGuidance(message: String): String {
    return when {
        message.contains("hardening", ignoreCase = true) ||
        message.contains("root", ignoreCase = true) ||
        message.contains("debugger", ignoreCase = true) ->
            "Use a safe device without root or debugger."
        message.contains("permission", ignoreCase = true) ->
            "Check app permissions, then retry."
        message.contains("security", ignoreCase = true) ->
            "Review access settings, then retry."
        message.contains("initialize", ignoreCase = true) ->
            "Retry startup first."
        else ->
            "Retry startup first."
    }
}

internal fun startupRecoveryActionHint(message: String): String {
    return when {
        message.contains("hardening", ignoreCase = true) ||
        message.contains("root", ignoreCase = true) ||
        message.contains("debugger", ignoreCase = true) ->
            "This device cannot run protected access safely."
        message.contains("permission", ignoreCase = true) ->
            "Open App settings if retry does not help."
        message.contains("security", ignoreCase = true) ->
            "Open settings if the issue repeats."
        message.contains("initialize", ignoreCase = true) ->
            "Retry should be enough first."
        else ->
            "Open settings if retry does not help."
    }
}