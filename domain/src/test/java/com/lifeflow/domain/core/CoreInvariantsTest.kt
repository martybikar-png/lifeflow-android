package com.lifeflow.domain.core

import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.TrustState
import org.junit.Test

class CoreInvariantsTest {

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-T1 COMPROMISED with Allowed throws violation`() {
        CoreInvariants.assertCompromisedNeverPermits(
            TrustState.COMPROMISED,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-T1 VERIFIED with Allowed does not throw`() {
        CoreInvariants.assertCompromisedNeverPermits(
            TrustState.VERIFIED,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-T2 DEGRADED save with Allowed throws violation`() {
        CoreInvariants.assertDegradedNeverPermitsWrite(
            TrustState.DEGRADED,
            DomainOperation.SAVE_IDENTITY,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-T2 DEGRADED delete with Allowed throws violation`() {
        CoreInvariants.assertDegradedNeverPermitsWrite(
            TrustState.DEGRADED,
            DomainOperation.DELETE_IDENTITY,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-T2 DEGRADED read with Allowed does not throw`() {
        CoreInvariants.assertDegradedNeverPermitsWrite(
            TrustState.DEGRADED,
            DomainOperation.READ_ACTIVE_IDENTITY,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-T2 VERIFIED write with Allowed does not throw`() {
        CoreInvariants.assertDegradedNeverPermitsWrite(
            TrustState.VERIFIED,
            DomainOperation.SAVE_IDENTITY,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-S1 unauthorized session with Allowed throws violation`() {
        CoreInvariants.assertExpiredSessionNeverProvides(
            false,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-S1 authorized session with Allowed does not throw`() {
        CoreInvariants.assertExpiredSessionNeverProvides(
            true,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-V1 vault read in COMPROMISED throws violation`() {
        CoreInvariants.assertVaultReadRequiresTrustAndSession(
            TrustState.COMPROMISED,
            true,
            DomainOperation.READ_IDENTITY_BY_ID
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-V1 vault read without session throws violation`() {
        CoreInvariants.assertVaultReadRequiresTrustAndSession(
            TrustState.VERIFIED,
            false,
            DomainOperation.READ_ACTIVE_IDENTITY
        )
    }

    @Test
    fun `INV-V1 vault read with VERIFIED and session does not throw`() {
        CoreInvariants.assertVaultReadRequiresTrustAndSession(
            TrustState.VERIFIED,
            true,
            DomainOperation.READ_ACTIVE_IDENTITY
        )
    }

    @Test
    fun `INV-V1 write operation does not trigger read invariant`() {
        CoreInvariants.assertVaultReadRequiresTrustAndSession(
            TrustState.COMPROMISED,
            false,
            DomainOperation.SAVE_IDENTITY
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-R1 recovery required with Allowed throws violation`() {
        CoreInvariants.assertRecoveryNeverBypassed(
            true,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-R1 no recovery with Allowed does not throw`() {
        CoreInvariants.assertRecoveryNeverBypassed(
            false,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-C1 commerce in safe minimal throws violation`() {
        CoreInvariants.assertNoCommerceInSafeMinimal(
            true,
            true
        )
    }

    @Test
    fun `INV-C1 commerce outside safe minimal does not throw`() {
        CoreInvariants.assertNoCommerceInSafeMinimal(
            false,
            true
        )
    }

    @Test
    fun `INV-C1 non commerce in safe minimal does not throw`() {
        CoreInvariants.assertNoCommerceInSafeMinimal(
            true,
            false
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-I1 integrity failure with Allowed throws violation`() {
        CoreInvariants.assertIntegrityFailureNeverContinues(
            true,
            AuthorizationResult.Allowed
        )
    }

    @Test
    fun `INV-I1 no integrity failure with Allowed does not throw`() {
        CoreInvariants.assertIntegrityFailureNeverContinues(
            false,
            AuthorizationResult.Allowed
        )
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-M1 same version throws violation`() {
        CoreInvariants.assertMonotonicVersion(5L, 5L)
    }

    @Test(expected = CoreInvariantViolation::class)
    fun `INV-M1 lower version throws violation`() {
        CoreInvariants.assertMonotonicVersion(5L, 4L)
    }

    @Test
    fun `INV-M1 higher version does not throw`() {
        CoreInvariants.assertMonotonicVersion(5L, 6L)
    }
}
