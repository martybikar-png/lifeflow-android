package com.lifeflow.security.hardening

import java.io.File

internal object SecurityTamperSignal {

    private val suspiciousArtifactPaths = setOf(
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/data/local/tmp/frida-gadget.so",
        "/system/bin/frida-server",
        "/system/xbin/frida-server"
    )

    private val suspiciousRuntimeMapTokens = setOf(
        "frida",
        "xposed",
        "substrate"
    )

    fun detect(findings: MutableList<String>): Boolean {
        val presentArtifactPaths = suspiciousArtifactPaths.filterTo(mutableSetOf()) { path ->
            File(path).exists()
        }
        val procSelfMaps = readProcSelfMaps()

        return detectFromSignals(
            presentArtifactPaths = presentArtifactPaths,
            procSelfMaps = procSelfMaps,
            findings = findings
        )
    }

    internal fun detectFromSignals(
        presentArtifactPaths: Set<String>,
        procSelfMaps: String?,
        findings: MutableList<String>
    ): Boolean {
        presentArtifactPaths.forEach { path ->
            findings += "Tamper: suspicious artifact path detected ($path)"
        }

        val normalizedMaps = procSelfMaps.orEmpty().lowercase()
        val detectedTokens = suspiciousRuntimeMapTokens.filterTo(mutableSetOf()) { token ->
            normalizedMaps.contains(token)
        }

        detectedTokens.forEach { token ->
            findings += "Tamper: suspicious runtime mapping detected ($token)"
        }

        return presentArtifactPaths.isNotEmpty() || detectedTokens.isNotEmpty()
    }

    private fun readProcSelfMaps(): String? {
        return try {
            val file = File("/proc/self/maps")
            if (file.exists()) file.readText() else null
        } catch (_: Throwable) {
            null
        }
    }
}
