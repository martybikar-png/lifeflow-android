package com.lifeflow

/**
 * Runtime (non-startup) security recovery controller.
 *
 * Purpose:
 * - keep non-startup rebuild policy out of LifeFlowAppRuntime
 * - react to concrete runtime compromise signals
 * - prevent automatic rebuild loops in a single process
 */
internal enum class RuntimeSecurityRecoverySignal {
    QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS,
    TRUST_COMPROMISED_ON_PROTECTED_ACCESS,
    TRUST_COMPROMISED_TRANSITION,
    EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST
}

internal data class RuntimeSecurityRecoveryPlan(
    val rebuildRequest: ProtectedRuntimeRebuildRequest? = null,
    val skipMessage: String? = null
)

internal object RuntimeSecurityRecoveryController {

    private const val MAX_AUTOMATIC_RUNTIME_SECURITY_REBUILDS_PER_PROCESS = 1

    fun planOnProtectedAccess(
        quickHardeningCompromised: Boolean,
        trustCompromised: Boolean,
        automaticRuntimeSecurityRebuildsUsedInProcess: Int
    ): RuntimeSecurityRecoveryPlan {
        val signal = when {
            trustCompromised ->
                RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_ON_PROTECTED_ACCESS

            quickHardeningCompromised ->
                RuntimeSecurityRecoverySignal.QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS

            else ->
                null
        } ?: return RuntimeSecurityRecoveryPlan()

        return planForSignal(
            signal = signal,
            automaticRuntimeSecurityRebuildsUsedInProcess =
                automaticRuntimeSecurityRebuildsUsedInProcess,
            logMessage = when (signal) {
                RuntimeSecurityRecoverySignal.QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS ->
                    "Protected runtime rebuild requested after quick hardening compromise during protected runtime access."

                RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_ON_PROTECTED_ACCESS ->
                    "Protected runtime rebuild requested after compromised trust state during protected runtime access."

                RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_TRANSITION ->
                    error("TRUST_COMPROMISED_TRANSITION is not expected in planOnProtectedAccess().")

                RuntimeSecurityRecoverySignal.EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST ->
                    error("EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST is not expected in planOnProtectedAccess().")
            }
        )
    }

    fun planOnTrustCompromisedTransition(
        automaticRuntimeSecurityRebuildsUsedInProcess: Int
    ): RuntimeSecurityRecoveryPlan {
        return planForSignal(
            signal = RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_TRANSITION,
            automaticRuntimeSecurityRebuildsUsedInProcess =
                automaticRuntimeSecurityRebuildsUsedInProcess,
            logMessage =
                "Protected runtime rebuild requested after runtime trust-state transition to COMPROMISED."
        )
    }

    fun planExplicitSecurityRecoveryRequest(
        logMessage: String,
        automaticRuntimeSecurityRebuildsUsedInProcess: Int
    ): RuntimeSecurityRecoveryPlan {
        val normalizedLogMessage = logMessage.trim().ifBlank {
            "Protected runtime rebuild requested by explicit runtime security recovery path."
        }

        return planForSignal(
            signal = RuntimeSecurityRecoverySignal.EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST,
            automaticRuntimeSecurityRebuildsUsedInProcess =
                automaticRuntimeSecurityRebuildsUsedInProcess,
            logMessage = normalizedLogMessage
        )
    }

    private fun planForSignal(
        signal: RuntimeSecurityRecoverySignal,
        automaticRuntimeSecurityRebuildsUsedInProcess: Int,
        logMessage: String
    ): RuntimeSecurityRecoveryPlan {
        if (automaticRuntimeSecurityRebuildsUsedInProcess >=
            MAX_AUTOMATIC_RUNTIME_SECURITY_REBUILDS_PER_PROCESS
        ) {
            return RuntimeSecurityRecoveryPlan(
                skipMessage =
                    "Automatic runtime protected rebuild skipped: ${signal.name} budget exhausted " +
                        "($automaticRuntimeSecurityRebuildsUsedInProcess/" +
                        "$MAX_AUTOMATIC_RUNTIME_SECURITY_REBUILDS_PER_PROCESS)."
            )
        }

        val rebuildSource = when (signal) {
            RuntimeSecurityRecoverySignal.EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST ->
                ProtectedRuntimeRebuildSource.EXPLICIT_SECURITY_RECOVERY

            RuntimeSecurityRecoverySignal.QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS,
            RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_ON_PROTECTED_ACCESS,
            RuntimeSecurityRecoverySignal.TRUST_COMPROMISED_TRANSITION ->
                ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL
        }

        return RuntimeSecurityRecoveryPlan(
            rebuildRequest = ProtectedRuntimeRebuildRequest(
                reason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
                rebuildSource = rebuildSource,
                recoverySignal = signal.name,
                logMessage = logMessage
            )
        )
    }
}
