package com.lifeflow.security

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException

internal object SecurityKeystoreFailureHandler {

    fun throwForFailure(
        action: RuleAction?,
        failureReason: String,
        genericMessage: String,
        throwable: Throwable
    ): Nothing {
        when {
            hasCause<UserNotAuthenticatedException>(throwable) -> {
                SecurityAccessSession.clear()
                throw SecurityException(
                    "$genericMessage. Recent biometric authentication is required.",
                    throwable
                )
            }

            hasCause<KeyPermanentlyInvalidatedException>(throwable) ||
                hasCause<UnrecoverableKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_RECOVERY_REQUIRED: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore key was invalidated. Reset vault is required.",
                    throwable
                )
            }

            hasCause<SecurityKeystorePostureException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_POSTURE_VIOLATION: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore security posture is not valid. Reset vault is required.",
                    throwable
                )
            }

            hasCause<InvalidKeyException>(throwable) -> {
                SecurityAccessSession.clear()
                SecurityRuleEngine.setTrustState(
                    TrustState.COMPROMISED,
                    reason = "KEYSTORE_INVALID_KEY: $failureReason"
                )
                throw SecurityException(
                    "$genericMessage. Keystore key is not operational. Reset vault is required.",
                    throwable
                )
            }

            else -> {
                action?.let {
                    SecurityRuleEngine.reportCryptoFailure(
                        action = it,
                        reason = failureReason,
                        throwable = throwable
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
