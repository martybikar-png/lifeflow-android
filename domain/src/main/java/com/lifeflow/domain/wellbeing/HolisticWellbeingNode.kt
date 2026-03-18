package com.lifeflow.domain.wellbeing

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

/**
 * HolisticWellbeingNode — biological interpretation layer.
 *
 * Input:  DigitalTwinState (raw metrics + availability)
 * Output: WellbeingAssessment (interpreted biological state)
 *
 * Zero Android dependencies. No coroutines.
 * Fail-closed: blocked or missing identity → UNAVAILABLE.
 */
class HolisticWellbeingNode {

    fun assess(state: DigitalTwinState): WellbeingAssessment {
        if (!state.identityInitialized) {
            return WellbeingAssessment(
                activityLevel = ActivityLevel.UNAVAILABLE,
                heartRateStatus = HeartRateStatus.UNAVAILABLE,
                overallReadiness = OverallReadiness.BLOCKED,
                notes = listOf("Identity not initialized: wellbeing assessment blocked.")
            )
        }

        val activityLevel = resolveActivityLevel(
            steps = state.stepsLast24h,
            availability = state.stepsAvailability
        )

        val heartRateStatus = resolveHeartRateStatus(
            bpm = state.avgHeartRateLast24h,
            availability = state.heartRateAvailability
        )

        val overallReadiness = resolveOverallReadiness(
            activityLevel = activityLevel,
            heartRateStatus = heartRateStatus,
            stepsAvailability = state.stepsAvailability,
            heartRateAvailability = state.heartRateAvailability
        )

        val notes = buildNotes(
            activityLevel = activityLevel,
            heartRateStatus = heartRateStatus,
            overallReadiness = overallReadiness
        )

        return WellbeingAssessment(
            activityLevel = activityLevel,
            heartRateStatus = heartRateStatus,
            overallReadiness = overallReadiness,
            notes = notes
        )
    }

    private fun resolveActivityLevel(
        steps: Long?,
        availability: DigitalTwinState.Availability
    ): ActivityLevel {
        return when (availability) {
            DigitalTwinState.Availability.BLOCKED -> ActivityLevel.UNAVAILABLE
            DigitalTwinState.Availability.PERMISSION_DENIED -> ActivityLevel.NO_ACCESS
            DigitalTwinState.Availability.UNKNOWN -> ActivityLevel.UNKNOWN
            DigitalTwinState.Availability.NO_DATA -> ActivityLevel.NO_DATA
            DigitalTwinState.Availability.OK -> when {
                steps == null -> ActivityLevel.NO_DATA
                steps >= 10_000 -> ActivityLevel.ACTIVE
                steps >= 5_000 -> ActivityLevel.MODERATE
                steps >= 1_000 -> ActivityLevel.LOW
                else -> ActivityLevel.SEDENTARY
            }
        }
    }

    private fun resolveHeartRateStatus(
        bpm: Long?,
        availability: DigitalTwinState.Availability
    ): HeartRateStatus {
        return when (availability) {
            DigitalTwinState.Availability.BLOCKED -> HeartRateStatus.UNAVAILABLE
            DigitalTwinState.Availability.PERMISSION_DENIED -> HeartRateStatus.NO_ACCESS
            DigitalTwinState.Availability.UNKNOWN -> HeartRateStatus.UNKNOWN
            DigitalTwinState.Availability.NO_DATA -> HeartRateStatus.NO_DATA
            DigitalTwinState.Availability.OK -> when {
                bpm == null -> HeartRateStatus.NO_DATA
                bpm < 40 -> HeartRateStatus.ABNORMAL_LOW
                bpm > 120 -> HeartRateStatus.ABNORMAL_HIGH
                bpm in 40..60 -> HeartRateStatus.RESTING_LOW
                bpm in 61..100 -> HeartRateStatus.NORMAL
                else -> HeartRateStatus.ELEVATED
            }
        }
    }

    private fun resolveOverallReadiness(
        activityLevel: ActivityLevel,
        heartRateStatus: HeartRateStatus,
        stepsAvailability: DigitalTwinState.Availability,
        heartRateAvailability: DigitalTwinState.Availability
    ): OverallReadiness {
        if (activityLevel == ActivityLevel.UNAVAILABLE ||
            heartRateStatus == HeartRateStatus.UNAVAILABLE
        ) return OverallReadiness.BLOCKED

        val noAccess = stepsAvailability == DigitalTwinState.Availability.PERMISSION_DENIED &&
                heartRateAvailability == DigitalTwinState.Availability.PERMISSION_DENIED
        if (noAccess) return OverallReadiness.NO_ACCESS

        val hasAnyData = stepsAvailability == DigitalTwinState.Availability.OK ||
                heartRateAvailability == DigitalTwinState.Availability.OK
        if (!hasAnyData) return OverallReadiness.INSUFFICIENT_DATA

        val heartAbnormal = heartRateStatus == HeartRateStatus.ABNORMAL_LOW ||
                heartRateStatus == HeartRateStatus.ABNORMAL_HIGH
        if (heartAbnormal) return OverallReadiness.ATTENTION_REQUIRED

        return when (activityLevel) {
            ActivityLevel.ACTIVE -> OverallReadiness.GOOD
            ActivityLevel.MODERATE -> OverallReadiness.FAIR
            ActivityLevel.LOW,
            ActivityLevel.SEDENTARY -> OverallReadiness.LOW
            else -> OverallReadiness.INSUFFICIENT_DATA
        }
    }

    private fun buildNotes(
        activityLevel: ActivityLevel,
        heartRateStatus: HeartRateStatus,
        overallReadiness: OverallReadiness
    ): List<String> {
        val notes = mutableListOf<String>()
        if (activityLevel == ActivityLevel.SEDENTARY)
            notes += "Activity level is very low. Consider light movement."
        if (heartRateStatus == HeartRateStatus.ABNORMAL_LOW)
            notes += "Resting heart rate is unusually low. Review if persistent."
        if (heartRateStatus == HeartRateStatus.ABNORMAL_HIGH)
            notes += "Resting heart rate is elevated. Review if persistent."
        if (overallReadiness == OverallReadiness.INSUFFICIENT_DATA)
            notes += "Not enough signals to produce a full assessment. Refresh later."
        return notes
    }
}

data class WellbeingAssessment(
    val activityLevel: ActivityLevel,
    val heartRateStatus: HeartRateStatus,
    val overallReadiness: OverallReadiness,
    val notes: List<String> = emptyList()
)

enum class ActivityLevel {
    UNAVAILABLE, NO_ACCESS, UNKNOWN, NO_DATA,
    SEDENTARY, LOW, MODERATE, ACTIVE
}

enum class HeartRateStatus {
    UNAVAILABLE, NO_ACCESS, UNKNOWN, NO_DATA,
    ABNORMAL_LOW, RESTING_LOW, NORMAL, ELEVATED, ABNORMAL_HIGH
}

enum class OverallReadiness {
    BLOCKED, NO_ACCESS, INSUFFICIENT_DATA,
    ATTENTION_REQUIRED, LOW, FAIR, GOOD
}