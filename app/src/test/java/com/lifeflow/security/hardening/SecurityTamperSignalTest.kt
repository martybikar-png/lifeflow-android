package com.lifeflow.security.hardening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityTamperSignalTest {

    @Test
    fun `artifact path signal is detected`() {
        val findings = mutableListOf<String>()

        val detected = SecurityTamperSignal.detectFromSignals(
            presentArtifactPaths = setOf("/data/local/tmp/frida-server"),
            procSelfMaps = null,
            findings = findings
        )

        assertTrue(detected)
        assertTrue(findings.any { it.contains("artifact path", ignoreCase = true) })
    }

    @Test
    fun `runtime map token signal is detected`() {
        val findings = mutableListOf<String>()

        val detected = SecurityTamperSignal.detectFromSignals(
            presentArtifactPaths = emptySet(),
            procSelfMaps = "/system/lib/libfrida-gadget.so",
            findings = findings
        )

        assertTrue(detected)
        assertTrue(findings.any { it.contains("runtime mapping", ignoreCase = true) })
    }

    @Test
    fun `no tamper signals stay clean`() {
        val findings = mutableListOf<String>()

        val detected = SecurityTamperSignal.detectFromSignals(
            presentArtifactPaths = emptySet(),
            procSelfMaps = null,
            findings = findings
        )

        assertFalse(detected)
        assertTrue(findings.isEmpty())
    }
}
