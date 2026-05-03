package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.AUTH_REQUIRED_USER_MESSAGE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainViewModelSessionForegroundInstrumentedTest {

    @Test
    fun sessionPollTick_whenAuthenticatedUiLosesAuthorization_triggersSingleFailClosedExpiry() {
        val evaluation = expiredSessionEvaluation()
        var notified: Boolean? = null
        var failClosedMessage: String? = null
        var clearSession: Boolean? = null
        var clearNotificationCalled = false

        handleMainViewModelSessionPollTick(
            securityEvaluation = evaluation,
            alreadyNotified = false,
            handleSessionExpiryIfNeeded = { alreadyNotified ->
                handleMainViewModelSessionExpiryIfNeeded(
                    alreadyNotified = alreadyNotified,
                    setSessionExpiryNotified = { notified = it },
                    failClosedAuthentication = { message, shouldClearSession ->
                        failClosedMessage = message
                        clearSession = shouldClearSession
                    }
                )
            },
            clearSessionExpiryNotification = {
                clearNotificationCalled = true
            }
        )

        assertEquals(true, notified)
        assertEquals(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE, failClosedMessage)
        assertEquals(true, clearSession)
        assertFalse(clearNotificationCalled)
    }

    @Test
    fun sessionPollTick_whenExpiryAlreadyNotified_doesNotRepeatFailClosed() {
        val evaluation = expiredSessionEvaluation()
        var notificationMutated = false
        var failClosedCalled = false

        handleMainViewModelSessionPollTick(
            securityEvaluation = evaluation,
            alreadyNotified = true,
            handleSessionExpiryIfNeeded = { alreadyNotified ->
                handleMainViewModelSessionExpiryIfNeeded(
                    alreadyNotified = alreadyNotified,
                    setSessionExpiryNotified = { notificationMutated = true },
                    failClosedAuthentication = { _, _ -> failClosedCalled = true }
                )
            },
            clearSessionExpiryNotification = {}
        )

        assertFalse(notificationMutated)
        assertFalse(failClosedCalled)
    }

    @Test
    fun sessionPollTick_whenSessionIsValid_clearsExpiryNotificationFlag() {
        var expiryHandlerCalled = false
        var clearNotificationCalled = false

        handleMainViewModelSessionPollTick(
            securityEvaluation = validSessionEvaluation(),
            alreadyNotified = true,
            handleSessionExpiryIfNeeded = {
                expiryHandlerCalled = true
            },
            clearSessionExpiryNotification = {
                clearNotificationCalled = true
            }
        )

        assertFalse(expiryHandlerCalled)
        assertTrue(clearNotificationCalled)
    }

    @Test
    fun pendingForegroundRefresh_consumesOnceAndResetsFlag() {
        var pending = true

        val first = consumeMainViewModelPendingForegroundRefresh(
            pendingForegroundRefresh = pending,
            updatePendingForegroundRefresh = { pending = it }
        )

        assertTrue(first)
        assertFalse(pending)

        val second = consumeMainViewModelPendingForegroundRefresh(
            pendingForegroundRefresh = pending,
            updatePendingForegroundRefresh = { pending = it }
        )

        assertFalse(second)
        assertFalse(pending)
    }

    private fun expiredSessionEvaluation(): MainViewModelSecurityEvaluation =
        MainViewModelSecurityEvaluation(
            canExposeProtectedUiData = false,
            canPerformProtectedWrite = false,
            runtimeEntryBlockMessage = AUTH_REQUIRED_USER_MESSAGE,
            shouldExpireSession = true
        )

    private fun validSessionEvaluation(): MainViewModelSecurityEvaluation =
        MainViewModelSecurityEvaluation(
            canExposeProtectedUiData = true,
            canPerformProtectedWrite = true,
            runtimeEntryBlockMessage = null,
            shouldExpireSession = false
        )
}
