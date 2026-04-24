package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.domain.security.TrustState
import com.lifeflow.security.AUTH_REQUIRED_USER_MESSAGE
import com.lifeflow.security.RECOVERY_REQUIRED_USER_MESSAGE
import com.lifeflow.security.SECURITY_COMPROMISED_USER_MESSAGE
import com.lifeflow.security.SECURITY_DEGRADED_USER_MESSAGE
import com.lifeflow.security.SECURITY_EMERGENCY_LIMITED_USER_MESSAGE
import com.lifeflow.security.SecurityLockedReason
import com.lifeflow.security.withDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainViewModelSecurityAndErrorLanguageInstrumentedTest {

    @Test
    fun securityEvaluation_allowsProtectedUi_onlyForAuthenticatedAuthorizedVerified() {
        val evaluation = mainViewModelSecurityEvaluation(
            isAuthenticatedUi = true,
            isAuthorized = true,
            trustState = TrustState.VERIFIED
        )

        assertTrue(evaluation.canExposeProtectedUiData)
        assertNull(evaluation.runtimeEntryBlockMessage)
        assertFalse(evaluation.shouldExpireSession)
    }

    @Test
    fun securityEvaluation_requiresFreshSession_whenAuthenticatedUiLosesAuthorization() {
        val evaluation = mainViewModelSecurityEvaluation(
            isAuthenticatedUi = true,
            isAuthorized = false,
            trustState = TrustState.VERIFIED
        )

        assertFalse(evaluation.canExposeProtectedUiData)
        assertEquals(AUTH_REQUIRED_USER_MESSAGE, evaluation.runtimeEntryBlockMessage)
        assertTrue(evaluation.shouldExpireSession)
    }

    @Test
    fun trustStateMessages_mapAuthenticatedStates_toFailClosedMessages() {
        assertEquals(
            SECURITY_COMPROMISED_USER_MESSAGE,
            mainViewModelTrustStateMessageOrNull(
                trustState = TrustState.COMPROMISED,
                isAuthenticatedUi = true
            )
        )

        assertEquals(
            SECURITY_DEGRADED_USER_MESSAGE,
            mainViewModelTrustStateMessageOrNull(
                trustState = TrustState.DEGRADED,
                isAuthenticatedUi = true
            )
        )

        assertEquals(
            SECURITY_EMERGENCY_LIMITED_USER_MESSAGE,
            mainViewModelTrustStateMessageOrNull(
                trustState = TrustState.EMERGENCY_LIMITED,
                isAuthenticatedUi = true
            )
        )

        assertNull(
            mainViewModelTrustStateMessageOrNull(
                trustState = TrustState.VERIFIED,
                isAuthenticatedUi = true
            )
        )
    }

    @Test
    fun lockedReasonMapping_returnsStableUserMessages() {
        assertEquals(
            RECOVERY_REQUIRED_USER_MESSAGE,
            mainViewModelLockedReasonToUserMessage(
                SecurityLockedReason.RECOVERY_REQUIRED.withDetail("detail")
            )
        )

        assertEquals(
            SECURITY_COMPROMISED_USER_MESSAGE,
            mainViewModelLockedReasonToUserMessage(
                SecurityLockedReason.COMPROMISED.withDetail("detail")
            )
        )

        assertEquals(
            AUTH_REQUIRED_USER_MESSAGE,
            mainViewModelLockedReasonToUserMessage(
                SecurityLockedReason.AUTH_REQUIRED.withDetail("detail")
            )
        )
    }

    @Test
    fun errorScreenContent_prefersResetVault_forRecoveryRequiredStates() {
        val content = resolveErrorScreenContent(
            message = "Security compromised. Reset vault is required before continuing.",
            resetRequired = false
        )

        assertEquals("A calmer recovery step is needed", content.guidanceTitle)
        assertEquals("Reset vault", content.buttonLabel)
        assertTrue(
            content.nextStepMessage.contains("Reset the vault", ignoreCase = true)
        )
    }

    @Test
    fun errorScreenContent_returnsAuthenticateAgain_forAuthenticationRequired() {
        val content = resolveErrorScreenContent(
            message = "Authentication required. Please authenticate again.",
            resetRequired = false
        )

        assertEquals("Authentication is needed to continue", content.guidanceTitle)
        assertEquals("Authenticate again", content.buttonLabel)
        assertTrue(
            content.nextStepMessage.contains("Authenticate again", ignoreCase = true)
        )
    }
}
