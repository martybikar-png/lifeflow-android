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

data class QuickCaptureLibraryItem(
    val id: String,
    val note: String
)

data class QuickCaptureLibraryPresentation(
    val infoBody: String,
    val markers: List<String>,
    val recentCaptures: List<QuickCaptureLibraryItem> = emptyList()
) {
    companion object {
        fun initial(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Saved notes appear here.",
                markers = listOf("Saved", "Local", "Clear"),
                recentCaptures = emptyList()
            )

        fun loading(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Loading saved notes.",
                markers = listOf("Loading", "Local", "Ready"),
                recentCaptures = emptyList()
            )

        fun unavailable(): QuickCaptureLibraryPresentation =
            QuickCaptureLibraryPresentation(
                infoBody = "Library needs verified access.",
                markers = listOf("Locked", "Local", "Retry"),
                recentCaptures = emptyList()
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

        loadQuickCaptureLibraryOnce(
            orchestrator = orchestrator,
            quickCaptureLibraryState = quickCaptureLibraryState,
            failClosedWithError = failClosedWithError,
            updateLastAction = updateLastAction,
            successMessage = "Capture library loaded."
        )
    }
}

internal fun launchMainViewModelQuickCaptureDelete(
    scope: CoroutineScope,
    orchestrator: LifeFlowOrchestrator,
    id: String,
    canPerformProtectedWriteNow: () -> Boolean,
    canExposeProtectedUiDataNow: () -> Boolean,
    quickCaptureLibraryState: MutableState<QuickCaptureLibraryPresentation>,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit
) {
    scope.launch {
        val cleanId = id.trim()
        if (cleanId.isBlank()) {
            updateLastAction("Quick capture delete failed: missing capture id.")
            return@launch
        }

        if (!canPerformProtectedWriteNow()) {
            updateLastAction("Quick capture needs verified trust before deleting.")
            return@launch
        }

        updateLastAction("Quick capture delete requested.")

        when (val result = orchestrator.deleteDiaryEntry(cleanId)) {
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
                    successMessage = "Quick capture deleted."
                )
            }

            is ActionResult.Error ->
                updateLastAction("Quick capture delete failed: ${result.message}")

            is ActionResult.Locked -> failClosedWithError(
                mainViewModelLockedReasonToUserMessage(result.reason),
                true
            )
        }
    }
}

internal suspend fun loadQuickCaptureLibraryOnce(
    orchestrator: LifeFlowOrchestrator,
    quickCaptureLibraryState: MutableState<QuickCaptureLibraryPresentation>,
    failClosedWithError: (String, Boolean) -> Unit,
    updateLastAction: (String) -> Unit,
    successMessage: String
) {
    when (val result = orchestrator.loadDiaryState(identityInitialized = true)) {
        is ActionResult.Success -> {
            quickCaptureLibraryState.value = result.value.toQuickCaptureLibraryPresentation()
            updateLastAction(successMessage)
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

private fun ShadowDiaryState.toQuickCaptureLibraryPresentation(): QuickCaptureLibraryPresentation {
    return when (readiness) {
        DiaryReadiness.BLOCKED ->
            QuickCaptureLibraryPresentation(
                infoBody = "Library locked.",
                markers = listOf("Locked", "Protected", "Retry"),
                recentCaptures = emptyList()
            )

        DiaryReadiness.EMPTY ->
            QuickCaptureLibraryPresentation(
                infoBody = "No notes yet.",
                markers = listOf("Empty", "Local", "Ready"),
                recentCaptures = emptyList()
            )

        DiaryReadiness.READY -> {
            val count = recentEntries.size
            val suffix = if (count == 1) "" else "s"
            val recentCaptures = recentEntries
                .map { entry -> entry.toQuickCaptureLibraryItem() }
            val recentSummary = recentCaptures
                .mapIndexed { index, item -> "${index + 1}. ${item.note.toCapturePreviewText()}" }
                .ifEmpty { listOf("No capture details yet.") }
                .joinToString(separator = "\n")

            QuickCaptureLibraryPresentation(
                infoBody = "$count saved note$suffix.\nRecent:\n$recentSummary",
                markers = listOf("$count Saved", "Local", "Clear"),
                recentCaptures = recentCaptures
            )
        }
    }
}

private fun DiaryEntry.toQuickCaptureLibraryItem(): QuickCaptureLibraryItem {
    val cleanNote = note.trim().ifBlank { formatDiarySignal(signal) }
    return QuickCaptureLibraryItem(
        id = id,
        note = cleanNote.take(96)
    )
}

private fun String.toCapturePreviewText(): String = take(42)

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

internal fun normalizeQuickCaptureNote(note: String): String =
    note
        .trim()
        .replace(Regex("\\s+"), " ")
        .ifBlank { "Quick capture" }
        .take(96)
