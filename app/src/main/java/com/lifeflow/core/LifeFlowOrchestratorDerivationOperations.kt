package com.lifeflow.core

import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.habits.AutonomousHabitsEngine
import com.lifeflow.domain.habits.HabitsState
import com.lifeflow.domain.insights.InsightsState
import com.lifeflow.domain.insights.QuantumInsightsEngine
import com.lifeflow.domain.timeline.AdaptiveTimelineEngine
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

class LifeFlowOrchestratorDerivationOperations {
    fun computeTimeline(
        assessment: WellbeingAssessment
    ): AdaptiveTimelineState {
        return AdaptiveTimelineEngine().compute(assessment)
    }

    fun computeHabits(
        timelineState: AdaptiveTimelineState
    ): HabitsState {
        return AutonomousHabitsEngine().compute(timelineState)
    }

    fun computeInsights(
        wellbeing: WellbeingAssessment,
        timeline: AdaptiveTimelineState,
        diary: ShadowDiaryState
    ): InsightsState {
        return QuantumInsightsEngine().compute(
            wellbeing = wellbeing,
            timeline = timeline,
            diary = diary
        )
    }
}

