package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PublicShellHorizontalPadding = 20.dp
private val PublicShellInfoTopGap = 44.dp
private val PublicShellInfoBodyGap = 8.dp
private val PublicShellInfoNoteGap = 10.dp
private val PublicShellInfoMarkerGap = 16.dp
private val PublicShellActionHorizontalPadding = 12.dp

@Composable
internal fun PublicShellInfoActionScreen(
    screenTitle: String,
    screenSubtitle: String,
    infoTitle: String,
    infoBody: String,
    infoNote: String = "",
    infoMarkers: List<String> = emptyList(),
    showGoldEdge: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ScreenContainer(
        title = screenTitle,
        subtitle = screenSubtitle,
        showGoldEdge = showGoldEdge,
        scrollContent = false
    ) {
        LifeFlowActionFrame(
            modifier = Modifier.padding(horizontal = PublicShellHorizontalPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(PublicShellInfoTopGap))

                if (infoTitle.isNotBlank()) {
                    Text(
                        text = infoTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (infoBody.isNotBlank()) {
                    Spacer(modifier = Modifier.height(PublicShellInfoBodyGap))

                    Text(
                        text = infoBody,
                        style = lifeFlowCardSummaryStyle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (infoMarkers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PublicShellInfoMarkerGap))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LifeFlowMarkerPill(markers = infoMarkers)
                    }
                }
                if (infoNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(PublicShellInfoNoteGap))

                    Text(
                        text = infoNote,
                        style = lifeFlowCardSummaryStyle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LifeFlowBottomAnchoredActions {
                content()
            }
        }
    }
}

@Composable
internal fun PublicShellActionPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PublicShellActionHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifeFlowActionItemSpacing)
    ) {
        content()
    }
}
