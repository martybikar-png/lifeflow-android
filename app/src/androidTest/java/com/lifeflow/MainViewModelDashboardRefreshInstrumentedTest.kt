package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainViewModelDashboardRefreshInstrumentedTest {

    @Test
    fun triggerRuntimeRefresh_whenFreeTier_refreshesPublicOnly() = runBlocking {
        var lastAction: String? = null
        var tierRefreshed = false
        var publicRefreshCount = 0
        var protectedRefreshCalled = false
        var authenticatedUiQueried = false

        triggerMainViewModelRuntimeRefresh(
            lastActionMessage = "Manual dashboard refresh requested.",
            updateLastAction = { lastAction = it },
            refreshTierAndBoundaryState = { tierRefreshed = true },
            isFreeTier = { true },
            refreshPublicHealthStateOnly = { publicRefreshCount += 1 },
            refreshProtectedSnapshot = { protectedRefreshCalled = true },
            isAuthenticatedUiNow = {
                authenticatedUiQueried = true
                true
            }
        )

        assertEquals("Manual dashboard refresh requested.", lastAction)
        assertTrue(tierRefreshed)
        assertEquals(1, publicRefreshCount)
        assertFalse(protectedRefreshCalled)
        assertFalse(authenticatedUiQueried)
    }

    @Test
    fun triggerRuntimeRefresh_whenProtectedTier_forwardsAuthenticatedIdentityState() = runBlocking {
        var lastAction: String? = null
        var publicRefreshCalled = false
        var protectedIdentityInitialized: Boolean? = null
        var authenticatedUiQueryCount = 0

        triggerMainViewModelRuntimeRefresh(
            lastActionMessage = "Returned to foreground; secure refresh requested.",
            updateLastAction = { lastAction = it },
            refreshTierAndBoundaryState = {},
            isFreeTier = { false },
            refreshPublicHealthStateOnly = { publicRefreshCalled = true },
            refreshProtectedSnapshot = { identityInitialized ->
                protectedIdentityInitialized = identityInitialized
            },
            isAuthenticatedUiNow = {
                authenticatedUiQueryCount += 1
                true
            }
        )

        assertEquals("Returned to foreground; secure refresh requested.", lastAction)
        assertFalse(publicRefreshCalled)
        assertEquals(true, protectedIdentityInitialized)
        assertEquals(1, authenticatedUiQueryCount)
    }

    @Test
    fun publicHealthStateWithMessage_keepsPublicStateAndWritesBlockedMessage() = runBlocking {
        var publicRefreshCount = 0
        var lastAction: String? = null

        refreshMainViewModelPublicHealthStateWithMessage(
            message = MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE,
            refreshPublicHealthStateOnly = { publicRefreshCount += 1 },
            updateLastAction = { lastAction = it }
        )

        assertEquals(1, publicRefreshCount)
        assertEquals(MAIN_VIEW_MODEL_REFRESH_BLOCKED_MESSAGE, lastAction)
    }

    @Test
    fun protectedSnapshot_whenRefreshThrows_routesToUnexpectedFailureHandler() = runBlocking {
        var handledUnexpectedFailure = false

        refreshMainViewModelProtectedSnapshot(
            identityInitialized = true,
            refreshWellbeingSnapshotSafe = {
                error("synthetic protected refresh failure")
            },
            handleUnexpectedProtectedRefreshFailure = {
                handledUnexpectedFailure = true
            }
        )

        assertTrue(handledUnexpectedFailure)
    }

    @Test
    fun unexpectedProtectedRefreshFailure_whenAuthenticated_failClosesAndClearsSession() {
        var publicStateRefreshed = false
        var failClosedMessage: String? = null
        var clearSession: Boolean? = null
        var lastAction: String? = null

        handleMainViewModelUnexpectedProtectedRefreshFailure(
            wasAuthenticated = true,
            refreshPublicHealthStateOnly = { publicStateRefreshed = true },
            failClosedWithError = { message, shouldClearSession ->
                failClosedMessage = message
                clearSession = shouldClearSession
            },
            updateLastAction = { lastAction = it }
        )

        assertTrue(publicStateRefreshed)
        assertEquals(MAIN_VIEW_MODEL_UNEXPECTED_REFRESH_FAILURE_MESSAGE, failClosedMessage)
        assertEquals(true, clearSession)
        assertNull(lastAction)
    }

    @Test
    fun unexpectedProtectedRefreshFailure_whenNotAuthenticated_keepsFailClosedErrorSilent() {
        var publicStateRefreshed = false
        var failClosedCalled = false
        var lastAction: String? = null

        handleMainViewModelUnexpectedProtectedRefreshFailure(
            wasAuthenticated = false,
            refreshPublicHealthStateOnly = { publicStateRefreshed = true },
            failClosedWithError = { _, _ -> failClosedCalled = true },
            updateLastAction = { lastAction = it }
        )

        assertTrue(publicStateRefreshed)
        assertFalse(failClosedCalled)
        assertEquals("Protected refresh failed unexpectedly.", lastAction)
    }
}
