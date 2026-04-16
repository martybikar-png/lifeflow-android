package com.lifeflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorScreenLanguageTest {

    @Test
    fun `session expired maps to protected session expired content`() {
        val content = resolveErrorScreenContent(
            message = MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE,
            resetRequired = false
        )

        assertEquals("Protected access needs a fresh session", content.guidanceTitle)
        assertEquals("Authenticate again", content.buttonLabel)
        assertTrue(content.guidanceMessage.contains("protected session", ignoreCase = true))
        assertTrue(content.nextStepMessage.contains("Authenticate again", ignoreCase = true))
    }

    @Test
    fun `authentication required maps to auth required content`() {
        val content = resolveErrorScreenContent(
            message = "Authentication required. Please authenticate again.",
            resetRequired = false
        )

        assertEquals("Authentication is needed to continue", content.guidanceTitle)
        assertEquals("Authenticate again", content.buttonLabel)
        assertTrue(content.guidanceMessage.contains("authentication", ignoreCase = true))
    }

    @Test
    fun `security degraded maps to degraded content`() {
        val content = resolveErrorScreenContent(
            message = "Security degraded. Please authenticate again.",
            resetRequired = false
        )

        assertEquals("A safer state check is needed", content.guidanceTitle)
        assertEquals("Authenticate again", content.buttonLabel)
        assertTrue(content.nextStepMessage.contains("fresh protected state", ignoreCase = true))
    }

    @Test
    fun `security compromised message maps to recovery content`() {
        val content = resolveErrorScreenContent(
            message = "Security compromised. Reset vault is required before continuing.",
            resetRequired = false
        )

        assertEquals("A calmer recovery step is needed", content.guidanceTitle)
        assertEquals("Reset vault", content.buttonLabel)
        assertTrue(content.nextStepMessage.contains("Reset the vault", ignoreCase = true))
    }

    @Test
    fun `explicit reset required forces recovery content`() {
        val content = resolveErrorScreenContent(
            message = MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE,
            resetRequired = true
        )

        assertEquals("A calmer recovery step is needed", content.guidanceTitle)
        assertEquals("Reset vault", content.buttonLabel)
    }

    @Test
    fun `requiresVaultReset matches compromised and reset-required messages`() {
        assertTrue(requiresVaultReset("Security compromised. Reset vault is required before continuing."))
        assertTrue(requiresVaultReset("A vault reset is required before continuing safely."))
        assertFalse(requiresVaultReset(MAIN_VIEW_MODEL_SESSION_EXPIRED_MESSAGE))
    }
}
