package com.lifeflow

import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.SignalIntensity
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun launchMainViewModelQuickCaptureSave(
    scope: CoroutineScope,
    orchestrator: LifeFlowOrchestrator,
    canPerformProtectedWriteNow: () -> Boolean,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    scope.launch {
        if (!canPerformProtectedWriteNow()) {
            updateLastAction("Quick capture needs verified trust before saving.")
            return@launch
        }

        updateLastAction("Quick capture save requested.")

        when (val result = orchestrator.saveDiaryEntry(createQuickCaptureDiaryEntry())) {
            is ActionResult.Success -> updateLastAction("Quick capture saved.")
            is ActionResult.Error -> updateLastAction("Quick capture failed: ${result.message}")
            is ActionResult.Locked -> failClosedWithError(
                mainViewModelLockedReasonToUserMessage(result.reason),
                true
            )
        }
    }
}

private fun createQuickCaptureDiaryEntry(): DiaryEntry =
    DiaryEntry(
        id = UUID.randomUUID().toString(),
        timestampEpochMillis = System.currentTimeMillis(),
        signal = DiarySignal.MOOD_NEUTRAL,
        intensity = SignalIntensity.SUBTLE,
        note = "Quick capture"
    )
