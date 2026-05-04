package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.LifeFlowOrchestrator
import kotlinx.coroutines.CoroutineScope

internal class MainViewModelCaptureGateway(
    private val scope: CoroutineScope,
    private val orchestrator: LifeFlowOrchestrator,
    private val quickCaptureLibrary: MutableState<QuickCaptureLibraryPresentation>,
    private val canPerformProtectedWriteNow: () -> Boolean,
    private val canExposeProtectedUiDataNow: () -> Boolean,
    private val failClosedWithError: (String, Boolean) -> Unit,
    private val updateLastAction: (String) -> Unit
) {
    fun saveQuickCaptureDraft(note: String) =
        launchMainViewModelQuickCaptureSave(
            scope = scope,
            orchestrator = orchestrator,
            note = note,
            canPerformProtectedWriteNow = canPerformProtectedWriteNow,
            failClosedWithError = failClosedWithError,
            updateLastAction = updateLastAction
        )

    fun loadQuickCaptureLibrary() =
        launchMainViewModelQuickCaptureLibraryLoad(
            scope,
            orchestrator,
            canExposeProtectedUiDataNow,
            quickCaptureLibrary,
            failClosedWithError,
            updateLastAction
        )

    fun deleteQuickCapture(id: String) =
        launchMainViewModelQuickCaptureDelete(
            scope,
            orchestrator,
            id,
            canPerformProtectedWriteNow,
            canExposeProtectedUiDataNow,
            quickCaptureLibrary,
            failClosedWithError,
            updateLastAction
        )

    fun updateQuickCapture(id: String, note: String) =
        launchMainViewModelQuickCaptureUpdate(
            scope,
            orchestrator,
            id,
            note,
            canPerformProtectedWriteNow,
            canExposeProtectedUiDataNow,
            quickCaptureLibrary,
            failClosedWithError,
            updateLastAction
        )
}
