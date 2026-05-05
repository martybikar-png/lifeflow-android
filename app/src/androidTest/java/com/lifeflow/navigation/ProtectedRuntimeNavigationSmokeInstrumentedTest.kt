package com.lifeflow.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lifeflow.ActiveRuntimeScreenSnapshot
import com.lifeflow.QuickCaptureLibraryPresentation
import com.lifeflow.UiState
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import org.junit.Rule
import org.junit.Test

class ProtectedRuntimeNavigationSmokeInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun protectedRuntimeCanOpenWellbeingAndReturnHome() {
        setProtectedRuntimeAtHome()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wellbeing").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Private wellbeing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("One calm next step.").assertIsDisplayed()
    }

    @Test
    fun protectedRuntimeCanOpenJournalAndReturnHome() {
        setProtectedRuntimeAtHome()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journal").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Private journal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("One calm next step.").assertIsDisplayed()
    }

    private fun setProtectedRuntimeAtHome() {
        composeTestRule.setContent {
            var activeRoute by remember {
                mutableStateOf(ProtectedHomeRoute)
            }

            ProtectedRuntimeRouteContent(
                activeRoute = activeRoute,
                screen = protectedRuntimeSmokeSnapshot(),
                onRouteChange = { route -> activeRoute = route },
                onBack = { activeRoute = ProtectedQuickCaptureRoute },
                onSaveQuickCapture = {},
                onLoadQuickCaptureLibrary = {},
                onDeleteQuickCapture = {},
                onUpdateQuickCapture = { _, _ -> },
                onAuthenticate = {},
                onGrantHealthPermissions = {},
                onOpenHealthConnectSettings = {},
                onRefreshNow = {},
                onUpgradeToCore = {}
            )
        }
    }
}

private fun protectedRuntimeSmokeSnapshot(): ActiveRuntimeScreenSnapshot =
    ActiveRuntimeScreenSnapshot(
        uiState = UiState.Authenticated,
        isAuthenticating = true,
        healthState = HealthConnectUiState.Available,
        digitalTwinState = null,
        wellbeingAssessment = null,
        requiredPermissions = emptySet(),
        grantedPermissions = emptySet(),
        stepsGranted = true,
        hrGranted = true,
        boundarySnapshot = MainBoundarySnapshot.initial(),
        freeTierMessage = "",
        quickCaptureLibrary = QuickCaptureLibraryPresentation.initial(),
        lastAction = "Protected navigation smoke."
    )
