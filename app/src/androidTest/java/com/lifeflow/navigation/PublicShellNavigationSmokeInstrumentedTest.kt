package com.lifeflow.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class PublicShellNavigationSmokeInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun publicShellCanOpenWellbeingAndReturnHome() {
        setPublicShellAtHome()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wellbeing").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Wellbeing preview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("One calm next step.").assertIsDisplayed()
    }

    @Test
    fun publicShellCanOpenJournalAndReturnHome() {
        setPublicShellAtHome()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journal").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Journal preview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("One calm next step.").assertIsDisplayed()
    }

    private fun setPublicShellAtHome() {
        composeTestRule.setContent {
            var activeRoute by remember {
                mutableStateOf(LifeFlowScreenMap.home.route)
            }

            PublicShellRouteContent(
                activeRoute = activeRoute,
                startAtHome = true,
                onRouteChange = { route -> activeRoute = route },
                onCompleteOnboarding = {
                    activeRoute = LifeFlowScreenMap.home.route
                }
            )
        }
    }
}
