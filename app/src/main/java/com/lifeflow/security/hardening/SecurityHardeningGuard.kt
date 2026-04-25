package com.lifeflow.security.hardening

import android.content.Context
import com.lifeflow.BuildConfig

/**
 * SecurityHardeningGuard — protective shell around LifeFlow core.
 *
 * Checks: Root, Debugger, Emulator, Instrumentation/Hooking detection, Installer trust,
 * tamper signals, APK signature verification, and runtime integrity.
 *
 * Fail-closed: CRITICAL finding blocks vault initialization.
 * DEGRADED: emulator and installer trust warnings are surfaced but not blocked.
 */
object SecurityHardeningGuard {

    data class HardeningReport(
        val rootDetected: Boolean,
        val debuggerDetected: Boolean,
        val emulatorDetected: Boolean,
        val instrumentationDetected: Boolean,
        val installerTrustWarningDetected: Boolean,
        val tamperSignalDetected: Boolean,
        val signatureInvalid: Boolean,
        val runtimeIntegrityFailed: Boolean,
        val findings: List<String>
    ) {
        val isCritical: Boolean
            get() = rootDetected || debuggerDetected || instrumentationDetected ||
                tamperSignalDetected || signatureInvalid || runtimeIntegrityFailed

        val isDegraded: Boolean
            get() = emulatorDetected || installerTrustWarningDetected

        val isClean: Boolean
            get() = !isCritical && !isDegraded
    }

    fun assess(context: Context): HardeningReport {
        val findings = mutableListOf<String>()

        val rootDetected = detectHardeningRoot(context, findings)
        val debuggerDetected = detectHardeningDebugger(findings)
        val emulatorDetected = detectHardeningEmulator(findings)
        val instrumentationDetected = detectHardeningInstrumentation(context, findings)
        val installerTrustWarningDetected = detectHardeningInstallerTrust(context, findings)
        val tamperSignalDetected = detectHardeningTamperSignal(findings)

        val signatureResult = ApkSignatureVerifier.verify(context)
        findings += signatureResult.findings

        val integrityResult = RuntimeIntegrityCheck.check()
        findings += integrityResult.findings

        return HardeningReport(
            rootDetected = rootDetected,
            debuggerDetected = debuggerDetected,
            emulatorDetected = emulatorDetected,
            instrumentationDetected = instrumentationDetected,
            installerTrustWarningDetected = installerTrustWarningDetected,
            tamperSignalDetected = tamperSignalDetected,
            signatureInvalid = !signatureResult.isValid,
            runtimeIntegrityFailed = !integrityResult.isClean,
            findings = findings
        )
    }

    /**
     * Quick integrity check suitable for runtime access paths.
     *
     * Release:
     * - fail-closed on debugger/tracer
     * - fail-closed on runtime tamper signals
     *
     * Debug:
     * - do not self-lock during local development
     */
    fun isCompromisedQuick(): Boolean {
        if (BuildConfig.DEBUG) return false
        if (RuntimeIntegrityCheck.isCompromised()) return true
        return SecurityTamperSignal.detect(mutableListOf())
    }

    internal fun detectInstrumentationFromSignals(
        installedPackages: Set<String>,
        resolvableClasses: Set<String>,
        findings: MutableList<String>
    ): Boolean {
        return detectHardeningInstrumentationFromSignals(
            installedPackages = installedPackages,
            resolvableClasses = resolvableClasses,
            findings = findings
        )
    }

    internal fun detectInstallerTrustFromSource(
        installerPackageName: String?,
        findings: MutableList<String>
    ): Boolean {
        return detectHardeningInstallerTrustFromSource(
            installerPackageName = installerPackageName,
            findings = findings
        )
    }
}
