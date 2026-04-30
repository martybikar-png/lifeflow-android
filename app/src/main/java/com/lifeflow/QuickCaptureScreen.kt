package com.lifeflow

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    PublicShellInfoActionScreen(
        screenTitle = "Quick Capture",
        screenSubtitle = "Save one thing quickly.",
        infoTitle = "Capture",
        infoBody = "Save one small thing.",
        infoMarkers = listOf("Fast", "Local", "Light"),
        infoNote = if (enrichedCaptureLocked) { enrichedCapturePresentation?.detailMessage ?: "Core required." } else { "" }
    ) {
        PublicShellActionPanel {
            LifeFlowPrimaryActionButton(
                label = "Start Capture",
                onClick = onPrimaryCapture,
                modifier = Modifier.fillMaxWidth()
            )

            LifeFlowSecondaryActionButton(
                label = "Library",
                onClick = onOpenCaptureLibrary,
                modifier = Modifier.fillMaxWidth()
            )

            if (enrichedCapturePresentation?.shouldShowUpgradeAction() == true) {
                LifeFlowSecondaryActionButton(
                    label = "Upgrade to Core",
                    onClick = onUpgradeToCore,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LifeFlowSecondaryActionButton(
                label = "Back",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            )
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
