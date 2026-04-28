package com.lifeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PublicShellHorizontalPadding = 20.dp
private val PublicShellInfoTopGap = 44.dp
private val PublicShellInfoBodyGap = 8.dp
private val PublicShellInfoNoteGap = 10.dp
private val PublicShellActionTopGap = 176.dp
private val PublicShellActionHorizontalPadding = 12.dp

@Composable
internal fun PublicShellInfoActionScreen(
    screenTitle: String,
    screenSubtitle: String,
    infoTitle: String,
    infoBody: String,
    infoNote: String = "",
    showGoldEdge: Boolean = true,
    actionTopGap: Dp = PublicShellActionTopGap,
    content: @Composable ColumnScope.() -> Unit
) {
    ScreenContainer(
        title = screenTitle,
        subtitle = screenSubtitle,
        showGoldEdge = showGoldEdge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PublicShellHorizontalPadding),
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

            if (infoNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(PublicShellInfoNoteGap))

                Text(
                    text = infoNote,
                    style = lifeFlowCardSummaryStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(actionTopGap))

            content()
        }
    }
}

@Composable
internal fun PublicShellActionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PublicShellActionHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}
