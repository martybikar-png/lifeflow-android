package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun QuickCaptureScreen(
    enrichedCapturePresentation: BoundaryPresentation? = null,
    onPrimaryCapture: () -> Unit = {},
    onOpenCaptureLibrary: () -> Unit = {},
    onUpgradeToCore: () -> Unit = {},
    onBackToHome: () -> Unit = {},
) {
    val enrichedCaptureLocked = enrichedCapturePresentation.isLockedLike()

    ScreenContainer(
        title = "Quick Capture",
        showBackButton = true,
        onBack = onBackToHome,
        showGoldEdge = true
    ) {
        LifeFlowSectionPanel(title = "Capture") {
            Text(
                text = "Save one small thing.",
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LifeFlowPrimaryActionButton(
                label = "Start Capture",
                onClick = onPrimaryCapture
            )

            Spacer(modifier = Modifier.height(6.dp))

            LifeFlowSecondaryActionButton(
                label = "Library",
                onClick = onOpenCaptureLibrary
            )

            if (enrichedCaptureLocked) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = enrichedCapturePresentation?.detailMessage ?: "Core required.",
                    style = lifeFlowCardSummaryStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (enrichedCapturePresentation?.shouldShowUpgradeAction() == true) {
                    Spacer(modifier = Modifier.height(6.dp))

                    LifeFlowSecondaryActionButton(
                        label = "Upgrade to Core",
                        onClick = onUpgradeToCore
                    )
                }
            }
        }
    }
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
