package com.lifeflow.security

import com.lifeflow.domain.security.CompromiseReason
import com.lifeflow.domain.security.LockReason
import com.lifeflow.domain.security.LockoutContext
import com.lifeflow.domain.security.RecoveryOption
import com.lifeflow.domain.security.SecurityLifecyclePort

class SecurityLifecyclePortAdapter : SecurityLifecyclePort {

    override fun onCompromised(reason: CompromiseReason) {
        SecurityAccessSession.clear()
        SecurityRuleEngine.setTrustState(
            TrustState.COMPROMISED,
            reason = "Lifecycle compromise: ${reason.name}"
        )
    }

    override fun onLockout(context: LockoutContext) {
        SecurityAccessSession.clear()

        val targetState = when (context.reason) {
            LockReason.COMPROMISED,
            LockReason.RECOVERY_REQUIRED -> TrustState.COMPROMISED

            LockReason.LOCKED_OUT -> context.trustState.toAppTrustState()
        }

        SecurityRuleEngine.setTrustState(
            targetState,
            reason = buildString {
                append("Lifecycle lockout: ")
                append(context.reason.name)
                context.attemptedOperation?.let {
                    append(" during ")
                    append(it.name)
                }
            }
        )
    }

    override fun recoveryOptions(): List<RecoveryOption> {
        return when (SecurityRuleEngine.getTrustState()) {
            TrustState.VERIFIED -> emptyList()

            TrustState.DEGRADED -> listOf(
                RecoveryOption.RETRY_AUTHENTICATION,
                RecoveryOption.RESTART_SECURE_SESSION
            )

            TrustState.COMPROMISED -> listOf(
                RecoveryOption.RESET_VAULT,
                RecoveryOption.CONTACT_SUPPORT
            )
        }
    }
}
