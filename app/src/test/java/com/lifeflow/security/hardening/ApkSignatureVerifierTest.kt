package com.lifeflow.security.hardening

import org.junit.Assert.*
import org.junit.Test

class ApkSignatureVerifierTest {

    @Test
    fun `signature result with null signature is invalid`() {
        val result = ApkSignatureVerifier.SignatureResult(
            isValid = false,
            actualSignature = null,
            findings = listOf("Signature: unable to retrieve APK signature")
        )
        
        assertFalse(result.isValid)
        assertNull(result.actualSignature)
        assertTrue(result.findings.isNotEmpty())
    }

    @Test
    fun `signature result with valid signature is valid`() {
        val result = ApkSignatureVerifier.SignatureResult(
            isValid = true,
            actualSignature = "ABC123DEF456",
            findings = emptyList()
        )
        
        assertTrue(result.isValid)
        assertNotNull(result.actualSignature)
        assertTrue(result.findings.isEmpty())
    }

    @Test
    fun `signature mismatch produces finding`() {
        val result = ApkSignatureVerifier.SignatureResult(
            isValid = false,
            actualSignature = "WRONG_SIGNATURE",
            findings = listOf("Signature: APK signature mismatch — possible repackaging detected")
        )
        
        assertFalse(result.isValid)
        assertTrue(result.findings.any { it.contains("mismatch") })
    }
}
