package com.lifeflow.security

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import com.lifeflow.domain.security.DomainOperation
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException

internal object SecurityKeystoreFailureHandler {

    fun throwForFailure(
        operation: DomainOperation?,
        failureReason: String,
        genericMessage: String,
        throwable: Throwable
    ): Nothing {
        when {
            hasCause<UserNotAuthenticatedException>(throwable) -> {
                SecurityAccessSession.clear()
                throw SecurityLockedException(
                    lockedReason = SecurityLockedReason.AUTH_REQUIRED
                        .withDetail(genericMessage),
                    cause = throwable
                )
            }

            hasCause<KeyPermanentlyInvalidatedException>(throwable) ||
                hasCause<UnrecoverableKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_RECOVERY_REQUIRED: $failureReason"
                )
                throw SecurityLockedException(
                    lockedReason = SecurityLockedReason.RECOVERY_REQUIRED
                        .withDetail(genericMessage),
                    cause = throwable
                )
            }

            hasCause<SecurityKeystorePostureException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_POSTURE_VIOLATION: $failureReason"
                )
                throw SecurityLockedException(
                    lockedReason = SecurityLockedReason.RECOVERY_REQUIRED
                        .withDetail(genericMessage),
                    cause = throwable
                )
            }

            hasCause<InvalidKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_INVALID_KEY: $failureReason"
                )
                throw SecurityLockedException(
                    lockedReason = SecurityLockedReason.RECOVERY_REQUIRED
                        .withDetail(genericMessage),
                    cause = throwable
                )
            }

            else -> {
                operation?.let {
                    SecurityRuleEngine.reportCryptoFailure(
                        operation = it,
                        reason = failureReason,
                        throwable = throwable
                    )
                    throw SecurityLockedException(
                        lockedReason = SecurityLockedReason.COMPROMISED
                            .withDetail(genericMessage),
                        cause = throwable
                    )
                }

                throw SecurityException(genericMessage, throwable)
            }
        }
    }

    private inline fun <reified T : Throwable> hasCause(
        throwable: Throwable
    ): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }
        return false
    }
}
