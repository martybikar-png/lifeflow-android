package com.lifeflow

import com.lifeflow.domain.security.TrustState
import org.junit.Assert.*
import org.junit.Test

class MainViewModelSecuritySnapshotTest {

    // ── canExposeProtectedUiData ──

    @Test
    fun `canExposeProtectedUiData returns true when all conditions met`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.canExposeProtectedUiData(UiState.Authenticated)

        assertTrue(result)
    }

    @Test
    fun `canExposeProtectedUiData returns false when not authorized`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.canExposeProtectedUiData(UiState.Authenticated)

        assertFalse(result)
    }

    @Test
    fun `canExposeProtectedUiData returns false when not authenticated ui`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.canExposeProtectedUiData(UiState.Loading)

        assertFalse(result)
    }

    @Test
    fun `canExposeProtectedUiData returns false when compromised`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.COMPROMISED
        )

        val result = snapshot.canExposeProtectedUiData(UiState.Authenticated)

        assertFalse(result)
    }

    // ── runtimeEntryBlockMessage ──

    @Test
    fun `runtimeEntryBlockMessage returns null when clean`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.runtimeEntryBlockMessage()

        assertNull(result)
    }

    @Test
    fun `runtimeEntryBlockMessage returns message when not authorized`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.runtimeEntryBlockMessage()

        assertNotNull(result)
    }

    @Test
    fun `runtimeEntryBlockMessage returns message when compromised`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.COMPROMISED
        )

        val result = snapshot.runtimeEntryBlockMessage()

        assertNotNull(result)
        assertTrue(result!!.contains("compromised", ignoreCase = true))
    }

    // ── shouldExpireSession ──

    @Test
    fun `shouldExpireSession returns true when authenticated but not authorized`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.shouldExpireSession(UiState.Authenticated)

        assertTrue(result)
    }

    @Test
    fun `shouldExpireSession returns false when authorized`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.shouldExpireSession(UiState.Authenticated)

        assertFalse(result)
    }

    @Test
    fun `shouldExpireSession returns false when not authenticated ui`() {
        val snapshot = MainViewModelSecuritySnapshot(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        val result = snapshot.shouldExpireSession(UiState.Loading)

        assertFalse(result)
    }
}
