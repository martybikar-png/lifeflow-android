package com.lifeflow

internal fun refreshMainViewModelRequiredPermissionsDefinition(
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    updateLastAction: (String) -> Unit
) {
    val refresh = wellbeingRuntime.refreshRequiredPermissionsDefinition()

    wellbeingState.requiredHealthPermissions.value = refresh.requiredPermissions
    wellbeingState.healthPermissionsInitError.value = refresh.initError
    updateLastAction(refresh.lastActionMessage)
}

internal fun refreshMainViewModelHealthConnectStatusSafe(
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    updateLastAction: (String) -> Unit
) {
    val refresh = wellbeingRuntime.refreshHealthConnectStatusSafe()

    wellbeingState.healthConnectState.value = refresh.healthConnectState
    updateLastAction(refresh.lastActionMessage)
}

internal fun refreshMainViewModelPublicHealthStateOnly(
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    updateLastAction: (String) -> Unit
) {
    refreshMainViewModelHealthConnectStatusSafe(
        wellbeingRuntime = wellbeingRuntime,
        wellbeingState = wellbeingState,
        updateLastAction = updateLastAction
    )
    refreshMainViewModelRequiredPermissionsDefinition(
        wellbeingRuntime = wellbeingRuntime,
        wellbeingState = wellbeingState,
        updateLastAction = updateLastAction
    )

    val update = mainViewModelPublicHealthStateOnlyUiUpdate(
        healthConnectState = wellbeingState.healthConnectState.value,
        requiredHealthPermissions = wellbeingState.requiredHealthPermissions.value,
        grantedHealthPermissions = wellbeingState.grantedHealthPermissions.value,
        healthPermissionsInitError = wellbeingState.healthPermissionsInitError.value
    )

    applyMainViewModelWellbeingUiUpdate(
        update = update,
        healthConnectStateState = wellbeingState.healthConnectState,
        requiredHealthPermissionsState = wellbeingState.requiredHealthPermissions,
        grantedHealthPermissionsState = wellbeingState.grantedHealthPermissions,
        healthPermissionsInitErrorState = wellbeingState.healthPermissionsInitError,
        digitalTwinStateState = wellbeingState.digitalTwinState,
        wellbeingAssessmentState = wellbeingState.wellbeingAssessment,
        updateLastAction = updateLastAction
    )
}

internal suspend fun refreshMainViewModelPublicHealthStateWithGrantedPermissions(
    wellbeingRuntime: MainViewModelWellbeingRuntime,
    wellbeingState: MainViewModelWellbeingState,
    updateLastAction: (String) -> Unit
) {
    refreshMainViewModelPublicHealthStateOnly(
        wellbeingRuntime = wellbeingRuntime,
        wellbeingState = wellbeingState,
        updateLastAction = updateLastAction
    )

    val grantedRefresh = wellbeingRuntime.refreshGrantedHealthPermissionsSafe()
    wellbeingState.grantedHealthPermissions.value = grantedRefresh.grantedPermissions
    updateLastAction(grantedRefresh.lastActionMessage)

    val update = mainViewModelPublicHealthStateOnlyUiUpdate(
        healthConnectState = wellbeingState.healthConnectState.value,
        requiredHealthPermissions = wellbeingState.requiredHealthPermissions.value,
        grantedHealthPermissions = wellbeingState.grantedHealthPermissions.value,
        healthPermissionsInitError = wellbeingState.healthPermissionsInitError.value
    )

    applyMainViewModelWellbeingUiUpdate(
        update = update,
        healthConnectStateState = wellbeingState.healthConnectState,
        requiredHealthPermissionsState = wellbeingState.requiredHealthPermissions,
        grantedHealthPermissionsState = wellbeingState.grantedHealthPermissions,
        healthPermissionsInitErrorState = wellbeingState.healthPermissionsInitError,
        digitalTwinStateState = wellbeingState.digitalTwinState,
        wellbeingAssessmentState = wellbeingState.wellbeingAssessment,
        updateLastAction = updateLastAction
    )
}

internal suspend fun refreshMainViewModelPublicHealthStateWithMessage(
    message: String,
    refreshPublicHealthStateOnly: suspend () -> Unit,
    updateLastAction: (String) -> Unit
) {
    refreshPublicHealthStateOnly()
    updateLastAction(message)
}
