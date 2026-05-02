package com.lifeflow

internal data class MainViewModelSecurityEvaluation(
    val canExposeProtectedUiData: Boolean,
    val canPerformProtectedWrite: Boolean,
    val runtimeEntryBlockMessage: String?,
    val shouldExpireSession: Boolean
)
