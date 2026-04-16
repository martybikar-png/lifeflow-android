package com.lifeflow

import com.lifeflow.domain.security.TrustState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MainViewModelAuthDelegateTest {

    private var sessionCleared = false
    private var uiCachesWiped = false
    private var sessionExpiryNotified: Boolean? = null
    private var lastUiStateError: String? = null
    private var lastAction: String? = null
    private var currentUiState: UiState = UiState.Loading
    private var mockSecuritySnapshot: MainViewModelSecuritySnapshot = createCleanSnapshot()

    private lateinit var delegate: MainViewModelAuthDelegate

    @Before
    fun setup() {
        sessionCleared = false
        uiCachesWiped = false
        sessionExpiryNotified = null
        lastUiStateError = null
        lastAction = null
        currentUiState = UiState.Loading
        mockSecuritySnapshot = createCleanSnapshot()

        val authRuntime = MainViewModelAuthRuntime(
            currentSecuritySnapshot = { mockSecuritySnapshot },
            currentUiState = { currentUiState }
        )

        delegate = MainViewModelAuthDelegate(
            authRuntime = authRuntime,
            clearSession = { sessionCleared = true },
            wipeUiCachesFailClosed = { uiCachesWiped = true },
            setSessionExpiryNotified = { sessionExpiryNotified = it },
            setUiStateError = { lastUiStateError = it },
            updateLastAction = { lastAction = it }
        )
    }

    private fun createCleanSnapshot(
        isAuthorized: Boolean = true,
        trustState: TrustState = TrustState.VERIFIED
    ): MainViewModelSecuritySnapshot {
        return MainViewModelSecuritySnapshot(
            isAuthorized = isAuthorized,
            trustState = trustState
        )
    }

    @Test
    fun `ensureRuntimeEntryAllowed returns true when authorized and verified`() {
        mockSecuritySnapshot = createCleanSnapshot(
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        val result = delegate.ensureRuntimeEntryAllowed()

        assertTrue(result)
        assertNull(lastUiStateError)
        assertFalse(sessionCleared)
    }

    @Test
    fun `ensureRuntimeEntryAllowed returns false and fails closed when not authorized`() {
        mockSecuritySnapshot = createCleanSnapshot(
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        val result = delegate.ensureRuntimeEntryAllowed()

        assertFalse(result)
        assertNotNull(lastUiStateError)
        assertTrue(sessionCleared)
        assertTrue(uiCachesWiped)
    }

    @Test
    fun `ensureRuntimeEntryAllowed returns false when trust state is COMPROMISED`() {
        mockSecuritySnapshot = createCleanSnapshot(
            isAuthorized = true,
            trustState = TrustState.COMPROMISED
        )

        val result = delegate.ensureRuntimeEntryAllowed()

        assertFalse(result)
        assertNotNull(lastUiStateError)
    }

    @Test
    fun `failClosedWithError clears session and wipes caches`() {
        delegate.failClosedWithError("Test error")

        assertTrue(sessionCleared)
        assertTrue(uiCachesWiped)
        assertEquals(false, sessionExpiryNotified)
        assertEquals("Test error", lastUiStateError)
        assertTrue(lastAction?.contains("Fail-closed") == true)
    }

    @Test
    fun `failClosedWithError without clearing session keeps session`() {
        delegate.failClosedWithError("Test error", clearSession = false)

        assertFalse(sessionCleared)
        assertTrue(uiCachesWiped)
    }

    @Test
    fun `handleAuthenticationError triggers fail-closed`() {
        delegate.handleAuthenticationError("Auth failed")

        assertTrue(sessionCleared)
        assertTrue(uiCachesWiped)
        assertEquals("Auth failed", lastUiStateError)
    }

    @Test
    fun `handleObservedTrustState with COMPROMISED triggers fail-closed`() {
        currentUiState = UiState.Authenticated

        delegate.handleObservedTrustState(TrustState.COMPROMISED)

        assertNotNull(lastUiStateError)
        assertTrue(sessionCleared)
    }

    @Test
    fun `handleObservedTrustState with VERIFIED in authenticated state is no-op`() {
        currentUiState = UiState.Authenticated

        delegate.handleObservedTrustState(TrustState.VERIFIED)

        assertNull(lastUiStateError)
        assertFalse(sessionCleared)
    }

    @Test
    fun `handleSessionExpiryIfNeeded does nothing when already notified`() {
        delegate.handleSessionExpiryIfNeeded(alreadyNotified = true)

        assertNull(sessionExpiryNotified)
        assertNull(lastUiStateError)
    }

    @Test
    fun `handleSessionExpiryIfNeeded triggers fail-closed when not notified`() {
        delegate.handleSessionExpiryIfNeeded(alreadyNotified = false)

        assertEquals(false, sessionExpiryNotified)
        assertNotNull(lastUiStateError)
        assertTrue(sessionCleared)
    }

    @Test
    fun `completeAuthenticationBootstrapSuccess calls markAuthenticated`() {
        var authenticated = false

        delegate.completeAuthenticationBootstrapSuccess { authenticated = true }

        assertTrue(authenticated)
        assertTrue(lastAction?.contains("unlocked") == true)
    }

    @Test
    fun `completeAuthenticationBootstrapLocked triggers fail-closed`() {
        delegate.completeAuthenticationBootstrapLocked("Locked out")

        assertTrue(sessionCleared)
        assertEquals("Locked out", lastUiStateError)
    }

    @Test
    fun `completeAuthenticationBootstrapError triggers fail-closed`() {
        delegate.completeAuthenticationBootstrapError("Bootstrap failed")

        assertTrue(sessionCleared)
        assertEquals("Bootstrap failed", lastUiStateError)
    }

    @Test
    fun `completeVaultReset success clears session and updates state`() {
        delegate.completeVaultReset(isSuccess = true, failureMessage = null)

        assertTrue(sessionCleared)
        assertTrue(uiCachesWiped)
        assertTrue(lastUiStateError?.contains("reset complete") == true)
    }

    @Test
    fun `completeVaultReset failure shows error message`() {
        delegate.completeVaultReset(isSuccess = false, failureMessage = "Disk error")

        assertTrue(sessionCleared)
        assertTrue(lastUiStateError?.contains("Disk error") == true)
    }
}
