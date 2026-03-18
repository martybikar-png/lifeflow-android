package com.lifeflow.core

import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class LifeFlowOrchestratorBootstrapTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    @Test
    fun `bootstrap returns AUTH_REQUIRED when no authorized session exists`() {
        val identityRepo = FakeIdentityRepository()
        val orchestrator = newTestLifeFlowOrchestrator(identityRepository = identityRepo)

        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is ActionResult.Success ->
                throw AssertionError("Expected Locked but got Success.")

            is ActionResult.Error ->
                throw AssertionError("Expected Locked but got Error: ${result.message}")

            is ActionResult.Locked -> {
                assertTrue(
                    "Expected AUTH_REQUIRED lock reason.",
                    result.reason.contains("AUTH_REQUIRED")
                )
            }
        }

        assertNull(identityRepo.activeIdentity)
        assertEquals(0, identityRepo.saveCalls)
    }

    @Test
    fun `bootstrap creates new active identity when session is authorized`() {
        forceResetSecurityStateForLifeFlowOrchestratorTests(
            state = TrustState.DEGRADED,
            reason = "Authorized bootstrap creation test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository()
        val orchestrator = newTestLifeFlowOrchestrator(identityRepository = identityRepo)

        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }

        assertUnitSuccess(result)
        assertNotNull(identityRepo.activeIdentity)
        assertEquals(1, identityRepo.saveCalls)
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun `bootstrap keeps existing identity and does not save duplicate`() {
        forceResetSecurityStateForLifeFlowOrchestratorTests(
            state = TrustState.DEGRADED,
            reason = "Existing identity bootstrap test"
        )
        SecurityAccessSession.grantDefault()

        val existing = LifeFlowIdentity(
            id = UUID.randomUUID(),
            createdAtEpochMillis = 123456789L,
            isActive = true
        )
        val identityRepo = FakeIdentityRepository(initialActive = existing)
        val orchestrator = newTestLifeFlowOrchestrator(identityRepository = identityRepo)

        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }

        assertUnitSuccess(result)
        assertEquals(existing.id, identityRepo.activeIdentity?.id)
        assertEquals(0, identityRepo.saveCalls)
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun `bootstrap returns COMPROMISED lock when trust state is compromised`() {
        forceResetSecurityStateForLifeFlowOrchestratorTests(
            state = TrustState.COMPROMISED,
            reason = "Compromised bootstrap lock test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository()
        val orchestrator = newTestLifeFlowOrchestrator(identityRepository = identityRepo)

        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is ActionResult.Success ->
                throw AssertionError("Expected Locked but got Success.")

            is ActionResult.Error ->
                throw AssertionError("Expected Locked but got Error: ${result.message}")

            is ActionResult.Locked -> {
                assertTrue(
                    "Expected COMPROMISED lock reason.",
                    result.reason.contains("COMPROMISED")
                )
            }
        }

        assertNull(identityRepo.activeIdentity)
        assertEquals(0, identityRepo.saveCalls)
    }

    @Test
    fun `bootstrap returns Error when repository save fails`() {
        forceResetSecurityStateForLifeFlowOrchestratorTests(
            state = TrustState.DEGRADED,
            reason = "Bootstrap error path test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository(throwsOnSave = true)
        val orchestrator = newTestLifeFlowOrchestrator(identityRepository = identityRepo)

        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is ActionResult.Success ->
                throw AssertionError("Expected Error but got Success.")

            is ActionResult.Locked ->
                throw AssertionError("Expected Error but got Locked: ${result.reason}")

            is ActionResult.Error -> {
                assertTrue(
                    "Expected bootstrap failure message.",
                    result.message.contains("save failed", ignoreCase = true)
                )
            }
        }

        assertNull(identityRepo.activeIdentity)
        assertEquals(1, identityRepo.saveCalls)
    }
}