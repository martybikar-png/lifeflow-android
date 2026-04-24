package com.lifeflow.security.hardening

import java.io.File

internal object SecurityTamperSignal {

    private val suspiciousArtifactPaths = setOf(
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/data/local/tmp/frida-gadget.so",
        "/data/local/tmp/libfrida-gadget.so",
        "/data/local/tmp/frida-agent.so",
        "/system/bin/frida-server",
        "/system/xbin/frida-server",
        "/system/lib/libfrida-gadget.so",
        "/system/lib64/libfrida-gadget.so"
    )

    private val suspiciousRuntimeMapTokens = setOf(
        "frida",
        "frida-agent",
        "frida-gadget",
        "libfrida",
        "xposed",
        "edxp",
        "lsposed",
        "riru",
        "zygisk",
        "substrate"
    )

    data class SignalSnapshot(
        val presentArtifactPaths: Set<String>,
        val detectedRuntimeMapTokens: Set<String>
    ) {
        val detected: Boolean
            get() = presentArtifactPaths.isNotEmpty() || detectedRuntimeMapTokens.isNotEmpty()
    }

    fun detect(findings: MutableList<String>): Boolean {
        val snapshot = snapshot()

        snapshot.presentArtifactPaths.forEach { path ->
            findings += "Tamper: suspicious artifact path detected ($path)"
        }

        snapshot.detectedRuntimeMapTokens.forEach { token ->
            findings += "Tamper: suspicious runtime mapping detected ($token)"
        }

        return snapshot.detected
    }

    fun snapshot(): SignalSnapshot {
        val presentArtifactPaths = suspiciousArtifactPaths.filterTo(linkedSetOf()) { path ->
            File(path).exists()
        }

        val normalizedMaps = readProcSelfMaps().orEmpty().lowercase()
        val detectedRuntimeMapTokens = suspiciousRuntimeMapTokens.filterTo(linkedSetOf()) { token ->
            normalizedMaps.contains(token)
        }

        return SignalSnapshot(
            presentArtifactPaths = presentArtifactPaths,
            detectedRuntimeMapTokens = detectedRuntimeMapTokens
        )
    }

    internal fun detectFromSignals(
        presentArtifactPaths: Set<String>,
        procSelfMaps: String?,
        findings: MutableList<String>
    ): Boolean {
        val snapshot = snapshotFromSignals(
            presentArtifactPaths = presentArtifactPaths,
            procSelfMaps = procSelfMaps
        )

        snapshot.presentArtifactPaths.forEach { path ->
            findings += "Tamper: suspicious artifact path detected ($path)"
        }

        snapshot.detectedRuntimeMapTokens.forEach { token ->
            findings += "Tamper: suspicious runtime mapping detected ($token)"
        }

        return snapshot.detected
    }

    internal fun snapshotFromSignals(
        presentArtifactPaths: Set<String>,
        procSelfMaps: String?
    ): SignalSnapshot {
        val normalizedMaps = procSelfMaps.orEmpty().lowercase()
        val detectedRuntimeMapTokens = suspiciousRuntimeMapTokens.filterTo(linkedSetOf()) { token ->
            normalizedMaps.contains(token)
        }

        return SignalSnapshot(
            presentArtifactPaths = presentArtifactPaths.toCollection(linkedSetOf()),
            detectedRuntimeMapTokens = detectedRuntimeMapTokens
        )
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
