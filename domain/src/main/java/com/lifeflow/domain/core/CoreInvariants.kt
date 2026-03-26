package com.lifeflow.domain.core

import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.TrustState

/**
 * CoreInvariants — formal invariants that must ALWAYS hold true.
 *
 * These are not guidelines. These are constitutional truths of the system.
 * Any violation is a critical architectural failure.
 *
 * Invariants are verified by:
 *   - compile-time contracts (types, sealed classes)
 *   - runtime assertions (this object)
 *   - adversarial test suite
 *
 * Change policy: CoreInvariants may only change via explicit
 * architectural review + ledger entry. Never silently.
 */
object CoreInvariants {

    // ── INV-T1: COMPROMISED never permits sensitive action ──

    fun assertCompromisedNeverPermits(
        trustState: TrustState,
        result: AuthorizationResult
    ) {
        if (trustState == TrustState.COMPROMISED && result is AuthorizationResult.Allowed) {
            throw CoreInvariantViolation(
                "INV-T1: COMPROMISED state returned Allowed. " +
                "COMPROMISED must never permit any sensitive action."
            )
        }
    }

    // ── INV-T2: DEGRADED never permits write operations ──

    fun assertDegradedNeverPermitsWrite(
        trustState: TrustState,
        operation: DomainOperation,
        result: AuthorizationResult
    ) {
        val isWrite = operation == DomainOperation.SAVE_IDENTITY ||
                operation == DomainOperation.DELETE_IDENTITY
        if (trustState == TrustState.DEGRADED && isWrite && result is AuthorizationResult.Allowed) {
            throw CoreInvariantViolation(
                "INV-T2: DEGRADED state returned Allowed for write operation $operation. " +
                "DEGRADED must never permit writes."
            )
        }
    }

    // ── INV-S1: Expired session never provides access ──

    fun assertExpiredSessionNeverProvides(
        sessionAuthorized: Boolean,
        result: AuthorizationResult
    ) {
        if (!sessionAuthorized && result is AuthorizationResult.Allowed) {
            throw CoreInvariantViolation(
                "INV-S1: Unauthorized session returned Allowed. " +
                "Expired or missing session must never provide access."
            )
        }
    }

    // ── INV-V1: Vault read never without valid trust+session ──

    fun assertVaultReadRequiresTrustAndSession(
        trustState: TrustState,
        sessionAuthorized: Boolean,
        operation: DomainOperation
    ) {
        val isRead = operation == DomainOperation.READ_IDENTITY_BY_ID ||
                operation == DomainOperation.READ_ACTIVE_IDENTITY
        if (isRead && trustState == TrustState.COMPROMISED) {
            throw CoreInvariantViolation(
                "INV-V1: Vault read attempted in COMPROMISED state. " +
                "Vault access requires non-compromised trust."
            )
        }
        if (isRead && !sessionAuthorized) {
            throw CoreInvariantViolation(
                "INV-V1: Vault read attempted without authorized session. " +
                "Vault access requires active session."
            )
        }
    }

    // ── INV-R1: Recovery/reset never silently bypassed ──

    fun assertRecoveryNeverBypassed(
        recoveryRequired: Boolean,
        result: AuthorizationResult
    ) {
        if (recoveryRequired && result is AuthorizationResult.Allowed) {
            throw CoreInvariantViolation(
                "INV-R1: Recovery-required state returned Allowed. " +
                "Recovery must never be silently bypassed."
            )
        }
    }

    // ── INV-P1: Policy decision exists only at authority point ──
    // (This is a design-time invariant, enforced by architecture review
    //  and the Single Policy Source Lock. No runtime check needed.)

    // ── INV-C1: Commerce never in safe minimal mode ──

    fun assertNoCommerceInSafeMinimal(
        safeMinimalMode: Boolean,
        isCommerceAction: Boolean
    ) {
        if (safeMinimalMode && isCommerceAction) {
            throw CoreInvariantViolation(
                "INV-C1: Commerce action attempted in SAFE_MINIMAL_MODE. " +
                "Commerce must never operate in safe minimal mode."
            )
        }
    }

    // ── INV-I1: Integrity failure never leads to optimistic continue ──

    fun assertIntegrityFailureNeverContinues(
        integrityFailed: Boolean,
        result: AuthorizationResult
    ) {
        if (integrityFailed && result is AuthorizationResult.Allowed) {
            throw CoreInvariantViolation(
                "INV-I1: Integrity failure returned Allowed. " +
                "Integrity failure must always fail-closed."
            )
        }
    }

    // ── INV-A1: Orchestrator is the sole authority gateway ──
    // (Design-time invariant: no module may call security/vault/identity
    //  directly. Enforced by module isolation + architecture review.)

    // ── INV-M1: Version must be monotonically increasing ──

    fun assertMonotonicVersion(previousVersion: Long, newVersion: Long) {
        if (newVersion <= previousVersion) {
            throw CoreInvariantViolation(
                "INV-M1: Version rollback detected " +
                "(previous=$previousVersion, new=$newVersion). " +
                "Versions must be strictly monotonically increasing."
            )
        }
    }
}

/**
 * Exception for core invariant violations.
 * This is always a critical failure — never catch and continue.
 */
class CoreInvariantViolation(message: String) : SecurityException(message)
