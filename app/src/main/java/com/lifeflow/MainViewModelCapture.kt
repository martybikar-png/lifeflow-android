package com.lifeflow

import androidx.compose.runtime.MutableState
import com.lifeflow.core.ActionResult
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.DiaryReadiness
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.diary.SignalIntensity
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class QuickCaptureLibraryPresentation(
    val infoBody: String,
    val markers: List<String>
) {
    companion object {
        fun initial(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Light captures appear here.",
                markers = listOf("Saved", "Light", "Clear")
            )

        fun loading(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Loading captures.",
                markers = listOf("Loading", "Local", "Secure")
            )

        fun unavailable(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Capture library unavailable.",
                markers = listOf("Locked", "Local", "Retry")
            )
    }
}

internal fun launchMainViewModelQuickCaptureSave(
    scope: CoroutineScope,
    orchestrator: LifeFlowOrchestrator,
    note: String,
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

        when (val result = orchestrator.saveDiaryEntry(createQuickCaptureDiaryEntry(note))) {
            is ActionResult.Success -> updateLastAction("Quick capture saved.")
            is ActionResult.Error -> updateLastAction("Quick capture failed: ${result.message}")
            is ActionResult.Locked -> failClosedWithError(
                mainViewModelLockedReasonToUserMessage(result.reason),
                true
            )
        }
    }
}

internal fun launchMainViewModelQuickCaptureLibraryLoad(
    scope: CoroutineScope,
    orchestrator: LifeFlowOrchestrator,
    canExposeProtectedUiDataNow: () -> Boolean,
    quickCaptureLibraryState: MutableState<QuickCaptureLibraryPresentation>,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    scope.launch {
        if (!canExposeProtectedUiDataNow()) {
            failClosedWithError(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE, true)
            return@launch
        }

        quickCaptureLibraryState.value = QuickCaptureLibraryPresentation.loading()
        updateLastAction("Capture library load requested.")

        when (val result = orchestrator.loadDiaryState(identityInitialized = true)) {
            is ActionResult.Success -> {
                quickCaptureLibraryState.value = result.value.toQuickCaptureLibraryPresentation()
                updateLastAction("Capture library loaded.")
            }

            is ActionResult.Error -> {
                quickCaptureLibraryState.value = QuickCaptureLibraryPresentation.unavailable()
                updateLastAction("Capture library failed: ${result.message}")
            }

            is ActionResult.Locked -> failClosedWithError(
                mainViewModelLockedReasonToUserMessage(result.reason),
                true
            )
        }
    }
}

private fun ShadowDiaryState.toQuickCaptureLibraryPresentation(): QuickCaptureLibraryPresentation {
    return when (readiness) {
        DiaryReadiness.BLOCKED ->
            QuickCaptureLibraryPresentation(
                infoBody = "Capture library locked.",
                markers = listOf("Locked", "Protected", "Retry")
            )

        DiaryReadiness.EMPTY ->
            QuickCaptureLibraryPresentation(
                infoBody = "No captures yet.",
                markers = listOf("Empty", "Local", "Ready")
            )

        DiaryReadiness.READY -> {
            val count = recentEntries.size
            val latest = recentEntries.firstOrNull()?.toDisplayText() ?: "No capture details yet."
            val suffix = if (count == 1) "" else "s"

            QuickCaptureLibraryPresentation(
                infoBody = "$count saved capture$suffix.\nLatest: $latest",
                markers = listOf("$count Saved", formatDiarySignal(dominantSignal), "Local")
            )
        }
    }
}

private fun DiaryEntry.toDisplayText(): String {
    val cleanNote = note.trim().ifBlank { formatDiarySignal(signal) }
    return cleanNote.take(42)
}

private fun formatDiarySignal(signal: DiarySignal?): String {
    return signal?.name
        ?.replace("_", " ")
        ?.lowercase()
        ?.replaceFirstChar { it.titlecase() }
        ?: "No signal"
}

private fun createQuickCaptureDiaryEntry(note: String): DiaryEntry =
    DiaryEntry(
        id = UUID.randomUUID().toString(),
        timestampEpochMillis = System.currentTimeMillis(),
        signal = DiarySignal.MOOD_NEUTRAL,
        intensity = SignalIntensity.SUBTLE,
        note = normalizeQuickCaptureNote(note)
    )

private fun normalizeQuickCaptureNote(note: String): String =
    note
        .trim()
        .replace(Regex("\\s+"), " ")
        .ifBlank { "Quick capture" }
        .take(96)
