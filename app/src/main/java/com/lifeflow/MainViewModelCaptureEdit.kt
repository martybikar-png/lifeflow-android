package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.SignalIntensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun launchMainViewModelQuickCaptureUpdate(
    scope: CoroutineScope,
    orchestrator: LifeFlowOrchestrator,
    id: String,
    note: String,
    canPerformProtectedWriteNow: () -> Boolean,
    canExposeProtectedUiDataNow: () -> Boolean,
    quickCaptureLibraryState: MutableState<QuickCaptureLibraryPresentation>,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    scope.launch {
        val cleanId = id.trim()
        if (cleanId.isBlank()) {
            updateLastAction("Quick capture edit failed: missing capture id.")
            return@launch
        }

        if (!canPerformProtectedWriteNow()) {
            updateLastAction("Quick capture needs verified trust before editing.")
            return@launch
        }

        updateLastAction("Quick capture edit requested.")

        when (val result = orchestrator.saveDiaryEntry(createEditedQuickCaptureDiaryEntry(cleanId, note))) {
            is ActionResult.Success -> {
                if (!canExposeProtectedUiDataNow()) {
                    failClosedWithError(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE, true)
                    return@launch
                }

                quickCaptureLibraryState.value = QuickCaptureLibraryPresentation.loading()
                loadQuickCaptureLibraryOnce(
                    orchestrator = orchestrator,
                    quickCaptureLibraryState = quickCaptureLibraryState,
                    failClosedWithError = failClosedWithError,
                    updateLastAction = updateLastAction,
                    successMessage = "Quick capture updated."
                )
            }

            is ActionResult.Error ->
                updateLastAction("Quick capture edit failed: ${result.message}")

            is ActionResult.Locked -> failClosedWithError(
                mainViewModelLockedReasonToUserMessage(result.reason),
                true
            )
        }
    }
}

private fun createEditedQuickCaptureDiaryEntry(
    id: String,
    note: String
): DiaryEntry =
    DiaryEntry(
        id = id,
        timestampEpochMillis = System.currentTimeMillis(),
        signal = DiarySignal.MOOD_NEUTRAL,
        intensity = SignalIntensity.SUBTLE,
        note = normalizeQuickCaptureNote(note)
    )
