package com.lifeflow.boundary

internal fun BoundaryPresentation?.isLockedLike(): Boolean {
    return this?.state == BoundaryPresentationState.LOCKED ||
        this?.state == BoundaryPresentationState.DENIED
}

internal fun BoundaryPresentation?.shouldShowUpgradeAction(): Boolean {
    val presentation = this ?: return false
    return presentation.isLockedLike() && presentation.showUpgradePrompt
}
