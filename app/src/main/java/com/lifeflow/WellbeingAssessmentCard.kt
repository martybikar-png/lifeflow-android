package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.wellbeing.WellbeingAssessment

@Composable
internal fun WellbeingAssessmentCard(
    wellbeingAssessment: WellbeingAssessment?,
    onOpenWellbeing: () -> Unit = {},
    onOpenJournal: () -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(WellbeingViewMode.Overall) }

    val metrics = wellbeingMetrics(
        selectedMode = selectedMode,
        wellbeingAssessment = wellbeingAssessment
    )

    Column {
        StatusCardShell(
            title = "Wellbeing",
            titleColor = MaterialTheme.colorScheme.onSurface,
            summary = wellbeingAssessmentSummary(selectedMode)
        ) {}

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WellbeingSelectorChip(
                    label = "Overall",
                    selected = selectedMode == WellbeingViewMode.Overall,
                    onClick = { selectedMode = WellbeingViewMode.Overall }
                )
                WellbeingSelectorChip(
                    label = "Activity",
                    selected = selectedMode == WellbeingViewMode.Activity,
                    onClick = { selectedMode = WellbeingViewMode.Activity }
                )
                WellbeingSelectorChip(
                    label = "Heart",
                    selected = selectedMode == WellbeingViewMode.Heart,
                    onClick = { selectedMode = WellbeingViewMode.Heart }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp)
                .lifeFlowRaisedPanelChrome(WellbeingGraphPanelShape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            WellbeingTrendChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                selectedMode = selectedMode,
                wellbeingAssessment = wellbeingAssessment
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        WellbeingLegendPanel(selectedMode = selectedMode)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WellbeingMetricCard(
                metric = metrics[0],
                width = 84.dp
            )
            WellbeingMetricCard(
                metric = metrics[1],
                width = 84.dp
            )
            WellbeingMetricCard(
                metric = metrics[2],
                width = 84.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LifeFlowPrimaryActionButton(
                label = "Open wellbeing",
                onClick = onOpenWellbeing,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Journal",
                onClick = onOpenJournal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}