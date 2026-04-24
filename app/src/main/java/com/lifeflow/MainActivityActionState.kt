package com.lifeflow

private const val NO_ACTION_RECORDED_MESSAGE = "No action recorded yet."

internal fun permissionResultMessage(grantedPermissions: Set<String>): String {
    return if (grantedPermissions.isEmpty()) {
        "HC callback: no permissions granted; refresh requested"
    } else {
        "HC callback: ${grantedPermissions.size} granted; refresh requested"
    }
}

internal fun requiresVaultReset(message: String): Boolean {
    return isRecoveryRequiredMessage(message)
}

internal fun resolveDisplayedLastAction(
    uiAction: String,
    viewModelAction: String
): String {
    val normalizedUiAction = normalizeAction(uiAction)
    val normalizedViewModelAction = normalizeAction(viewModelAction)

    return when {
        normalizedUiAction == null && normalizedViewModelAction == null ->
            NO_ACTION_RECORDED_MESSAGE

        normalizedUiAction != null && normalizedViewModelAction == null ->
            normalizedUiAction

        normalizedUiAction == null && normalizedViewModelAction != null ->
            normalizedViewModelAction

        normalizedUiAction == normalizedViewModelAction ->
            normalizedUiAction ?: NO_ACTION_RECORDED_MESSAGE

        else ->
            "$normalizedUiAction → $normalizedViewModelAction"
    }
}

private fun normalizeAction(action: String): String? {
    return action.takeIf {
        it.isNotBlank() && it != NO_ACTION_RECORDED_MESSAGE
    }
}
