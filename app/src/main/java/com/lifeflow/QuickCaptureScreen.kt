package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.boundary.BoundaryEntitlementSource
import com.lifeflow.boundary.BoundaryPresentation
import com.lifeflow.boundary.BoundaryPresentationState
import com.lifeflow.boundary.isLockedLike
import com.lifeflow.boundary.shouldShowUpgradeAction
import com.lifeflow.domain.core.boundary.BoundaryAuditExpectation
import com.lifeflow.domain.core.boundary.EntitlementStatus

private val QuickCaptureActionRowLargeGap = 58.dp
private val QuickCaptureActionColumnGap = 16.dp

@Composable
fun QuickCaptureScreen(
    enrichedCapturePresentation: BoundaryPresentation? = null,
    onPrimaryCapture: () -> Unit = {},
    onOpenCaptureLibrary: () -> Unit = {},
    onUpgradeToCore: () -> Unit = {},
    statusMessage: String = "",
    onBackToHome: () -> Unit = {},
) {
    val enrichedCaptureLocked = enrichedCapturePresentation.isLockedLike()
    val showUpgradeAction = enrichedCapturePresentation?.shouldShowUpgradeAction() == true

    PublicShellInfoActionScreen(
        screenTitle = "Quick Capture",
        screenSubtitle = "Start with one clean note.",
        infoTitle = "Quick capture",
        infoBody = "Save it before it fades.",
        infoMarkers = listOf("Simple", "Local", "Ready"),
        infoNote = captureInfoNote(
            primaryNote = if (enrichedCaptureLocked) {
                enrichedCapturePresentation?.detailMessage ?: "Core required."
            } else {
                ""
            },
            statusMessage = statusMessage
        ),
        actionAnchor = LifeFlowActionAnchor.LoginLowerTwoRows
    ) {
        PublicShellActionPanel {
            if (showUpgradeAction) {
                QuickCaptureActionsWithUpgrade(
                    onPrimaryCapture = onPrimaryCapture,
                    onOpenCaptureLibrary = onOpenCaptureLibrary,
                    onUpgradeToCore = onUpgradeToCore,
                    onBackToHome = onBackToHome
                )
            } else {
                QuickCaptureActionsBase(
                    onPrimaryCapture = onPrimaryCapture,
                    onOpenCaptureLibrary = onOpenCaptureLibrary,
                    onBackToHome = onBackToHome
                )
            }
        }
    }
}

@Composable
private fun QuickCaptureActionsWithUpgrade(
    onPrimaryCapture: () -> Unit,
    onOpenCaptureLibrary: () -> Unit,
    onUpgradeToCore: () -> Unit,
    onBackToHome: () -> Unit
) {
    QuickCaptureActionRow {
        LifeFlowHomePrimaryActionButton(
            label = "New capture",
            onClick = onPrimaryCapture,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Open Library",
            onClick = onOpenCaptureLibrary,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(QuickCaptureActionRowLargeGap))

    QuickCaptureActionRow {
        LifeFlowHomeSecondaryActionButton(
            label = "Upgrade",
            onClick = onUpgradeToCore,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Back",
            onClick = onBackToHome,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun QuickCaptureActionsBase(
    onPrimaryCapture: () -> Unit,
    onOpenCaptureLibrary: () -> Unit,
    onBackToHome: () -> Unit
) {
    QuickCaptureActionRow {
        LifeFlowHomePrimaryActionButton(
            label = "New capture",
            onClick = onPrimaryCapture,
            modifier = Modifier.weight(1f)
        )
        LifeFlowHomeSecondaryActionButton(
            label = "Open Library",
            onClick = onOpenCaptureLibrary,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(QuickCaptureActionRowLargeGap))

    QuickCaptureActionRow {
        LifeFlowHomeSecondaryActionButton(
            label = "Back",
            onClick = onBackToHome,
            modifier = Modifier
                .weight(1f)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickCaptureActionRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QuickCaptureActionColumnGap),
        content = content
    )
}

internal fun publicShellEnrichedCapturePresentation(): BoundaryPresentation {
    return BoundaryPresentation(
        boundaryKey = "action.capture.enriched",
        title = "Enriched Capture Analysis",
        state = BoundaryPresentationState.LOCKED,
        showUpgradePrompt = true,
        showLockedBadge = true,
        allowUserActionAudit = false,
        messageCode = "core_enriched_capture_required",
        owner = "Capture",
        entitlementStatus = EntitlementStatus.ACTIVE,
        entitlementSource = BoundaryEntitlementSource.PUBLIC_SHELL,
        isGraceAccess = false,
        auditExpectation = BoundaryAuditExpectation.NONE,
        detailMessage = "Core required. Enriched capture stays locked in public shell."
    )
}
