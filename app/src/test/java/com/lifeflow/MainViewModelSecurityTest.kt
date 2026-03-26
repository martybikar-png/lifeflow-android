package com.lifeflow

import com.lifeflow.domain.security.TrustState
import org.junit.Assert.*
import org.junit.Test

class MainViewModelSecurityTest {

    // ── canMainViewModelExposeProtectedUiData ──

    @Test
    fun `canExposeProtectedUiData true when authenticated, authorized, and verified`() {
        val result = canMainViewModelExposeProtectedUiData(
            uiState = UiState.Authenticated,
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        assertTrue(result)
    }

    @Test
    fun `canExposeProtectedUiData false when not authenticated`() {
        val result = canMainViewModelExposeProtectedUiData(
            uiState = UiState.Loading,
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        assertFalse(result)
    }

    @Test
    fun `canExposeProtectedUiData false when not authorized`() {
        val result = canMainViewModelExposeProtectedUiData(
            uiState = UiState.Authenticated,
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        assertFalse(result)
    }

    @Test
    fun `canExposeProtectedUiData false when trust state compromised`() {
        val result = canMainViewModelExposeProtectedUiData(
            uiState = UiState.Authenticated,
            isAuthorized = true,
            trustState = TrustState.COMPROMISED
        )

        assertFalse(result)
    }

    @Test
    fun `canExposeProtectedUiData false when trust state degraded`() {
        val result = canMainViewModelExposeProtectedUiData(
            uiState = UiState.Authenticated,
            isAuthorized = true,
            trustState = TrustState.DEGRADED
        )

        assertFalse(result)
    }

    // ── mainViewModelRuntimeEntryBlockMessage ──

    @Test
    fun `runtimeEntryBlockMessage returns null when authorized and verified`() {
        val result = mainViewModelRuntimeEntryBlockMessage(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        assertNull(result)
    }

    @Test
    fun `runtimeEntryBlockMessage returns auth message when not authorized`() {
        val result = mainViewModelRuntimeEntryBlockMessage(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        assertNotNull(result)
        assertTrue(result!!.contains("auth", ignoreCase = true))
    }

    @Test
    fun `runtimeEntryBlockMessage returns compromised message when compromised`() {
        val result = mainViewModelRuntimeEntryBlockMessage(
            isAuthorized = true,
            trustState = TrustState.COMPROMISED
        )

        assertNotNull(result)
        assertTrue(result!!.contains("compromised", ignoreCase = true))
    }

    @Test
    fun `runtimeEntryBlockMessage returns degraded message when degraded`() {
        val result = mainViewModelRuntimeEntryBlockMessage(
            isAuthorized = true,
            trustState = TrustState.DEGRADED
        )

        assertNotNull(result)
        assertTrue(result!!.contains("degraded", ignoreCase = true))
    }

    // ── resolveMainViewModelTrustUpdate ──

    @Test
    fun `resolveMainViewModelTrustUpdate returns FailClosed for COMPROMISED`() {
        val result = resolveMainViewModelTrustUpdate(
            trustState = TrustState.COMPROMISED,
            uiState = UiState.Authenticated
        )

        assertTrue(result is MainViewModelTrustUpdate.FailClosed)
    }

    @Test
    fun `resolveMainViewModelTrustUpdate returns FailClosed for DEGRADED when authenticated`() {
        val result = resolveMainViewModelTrustUpdate(
            trustState = TrustState.DEGRADED,
            uiState = UiState.Authenticated
        )

        assertTrue(result is MainViewModelTrustUpdate.FailClosed)
    }

    @Test
    fun `resolveMainViewModelTrustUpdate returns NoOp for DEGRADED when not authenticated`() {
        val result = resolveMainViewModelTrustUpdate(
            trustState = TrustState.DEGRADED,
            uiState = UiState.Loading
        )

        assertTrue(result is MainViewModelTrustUpdate.NoOp)
    }

    @Test
    fun `resolveMainViewModelTrustUpdate returns LastAction for VERIFIED when authenticated`() {
        val result = resolveMainViewModelTrustUpdate(
            trustState = TrustState.VERIFIED,
            uiState = UiState.Authenticated
        )

        assertTrue(result is MainViewModelTrustUpdate.LastAction)
    }

    @Test
    fun `resolveMainViewModelTrustUpdate returns NoOp for VERIFIED when not authenticated`() {
        val result = resolveMainViewModelTrustUpdate(
            trustState = TrustState.VERIFIED,
            uiState = UiState.Loading
        )

        assertTrue(result is MainViewModelTrustUpdate.NoOp)
    }

    // ── shouldMainViewModelExpireSession ──

    @Test
    fun `shouldExpireSession true when authenticated and not authorized`() {
        val result = shouldMainViewModelExpireSession(
            uiState = UiState.Authenticated,
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        assertTrue(result)
    }

    @Test
    fun `shouldExpireSession false when not authenticated`() {
        val result = shouldMainViewModelExpireSession(
            uiState = UiState.Loading,
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        assertFalse(result)
    }

    @Test
    fun `shouldExpireSession false when authorized`() {
        val result = shouldMainViewModelExpireSession(
            uiState = UiState.Authenticated,
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        assertFalse(result)
    }

    // ── mainViewModelLockedReasonToUserMessage ──

    @Test
    fun `lockedReasonToUserMessage handles COMPROMISED prefix`() {
        val result = mainViewModelLockedReasonToUserMessage("COMPROMISED: some detail")

        assertTrue(result.contains("compromised", ignoreCase = true))
    }

    @Test
    fun `lockedReasonToUserMessage handles AUTH_REQUIRED prefix`() {
        val result = mainViewModelLockedReasonToUserMessage("AUTH_REQUIRED: some detail")

        assertTrue(result.contains("auth", ignoreCase = true))
    }

    @Test
    fun `lockedReasonToUserMessage handles blank reason`() {
        val result = mainViewModelLockedReasonToUserMessage("")

        assertTrue(result.contains("locked", ignoreCase = true))
    }

    @Test
    fun `lockedReasonToUserMessage returns original for unknown reason`() {
        val result = mainViewModelLockedReasonToUserMessage("Custom error message")

        assertEquals("Custom error message", result)
    }
}
