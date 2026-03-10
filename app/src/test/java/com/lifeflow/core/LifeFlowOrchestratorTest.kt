package com.lifeflow.core

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class LifeFlowOrchestratorTest {

    private val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
    private val heartRatePermission = HealthPermission.getReadPermission(HeartRateRecord::class)

    @Before
    fun resetSecurityBaseline() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "LifeFlowOrchestratorTest baseline reset"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    @Test
    fun `returns UNKNOWN when Health Connect is not available`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.NotInstalled,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 1234L,
                avgHeartRateValue = 72.0
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns UNKNOWN when required permission set is empty`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = emptySet(),
                grantedPermissionsValue = emptySet(),
                stepsValue = 1234L,
                avgHeartRateValue = 72.0
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `resolves steps granted and heart rate denied independently`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission),
                stepsValue = 2500L,
                avgHeartRateValue = 68.0
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(2500L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)

        assertTrue(
            "Expected diagnostic note for denied heart-rate permission.",
            state.notes.any { it.contains("permission denied", ignoreCase = true) }
        )
    }

    @Test
    fun `resolves steps denied and heart rate granted independently`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(heartRatePermission),
                stepsValue = 2500L,
                avgHeartRateValue = 68.0
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, state.stepsAvailability)
        assertNull(state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.OK, state.heartRateAvailability)
        assertEquals(68L, state.avgHeartRateLast24h)
    }

    @Test
    fun `keeps zero steps as OK and maps missing heart rate to NO_DATA`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 0L,
                avgHeartRateValue = null
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(0L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns NO_DATA for both metrics when permissions are granted but no usable data exists`() {
        val orchestrator = newOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = -1L,
                avgHeartRateValue = 0.0
            )
        )

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns Error when required permissions lookup throws`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0,
            throwsOnRequiredPermissions = true
        )
        val orchestrator = newOrchestrator(repo)

        val result = orchestrator.requiredHealthPermissionsSafe()

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success ->
                throw AssertionError("Expected Error but got Success: ${result.value}")

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Error but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error -> {
                assertTrue(
                    "Expected IllegalStateException in error message.",
                    result.message.contains("IllegalStateException")
                )
            }
        }
    }

    @Test
    fun `returns empty set when granted permissions lookup throws`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0,
            throwsOnGrantedPermissions = true
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend { orchestrator.grantedHealthPermissionsSafe() }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                assertTrue(
                    "Expected deterministic empty set when granted permission lookup fails.",
                    result.value.isEmpty()
                )
            }

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    @Test
    fun `resolves steps metric when only steps permission is required`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission),
            grantedPermissionsValue = setOf(stepsPermission),
            stepsValue = 4321L,
            avgHeartRateValue = 77.0
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(4321L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(1, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `resolves heart rate metric when only heart rate permission is required`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(heartRatePermission),
            grantedPermissionsValue = setOf(heartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertNull(state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.OK, state.heartRateAvailability)
        assertEquals(72L, state.avgHeartRateLast24h)

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(1, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `returns UNKNOWN for both metrics when permission snapshot cannot be resolved`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnRequiredPermissions = true
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `maps metric read failures to NO_DATA when permissions are granted`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnReadSteps = true,
            throwsOnReadHeartRate = true
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend { orchestrator.refreshTwinBestEffort(identityInitialized = true) }

        val state = assertSuccess(result)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(1, repo.readTotalStepsCalls)
        assertEquals(1, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `refreshWellbeingSnapshot returns unified snapshot when health connect is available and permissions are granted`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                val snapshot = result.value
                assertEquals(HealthConnectUiState.Available, snapshot.healthConnectState)
                assertEquals(
                    setOf(stepsPermission, heartRatePermission),
                    snapshot.requiredPermissions
                )
                assertEquals(
                    setOf(stepsPermission, heartRatePermission),
                    snapshot.grantedPermissions
                )
                assertEquals(true, snapshot.stepsPermissionGranted)
                assertEquals(true, snapshot.heartRatePermissionGranted)

                val twin = snapshot.digitalTwinState
                assertTrue(twin.identityInitialized)
                assertEquals(4321L, twin.stepsLast24h)
                assertEquals(72L, twin.avgHeartRateLast24h)
                assertEquals(DigitalTwinState.Availability.OK, twin.stepsAvailability)
                assertEquals(DigitalTwinState.Availability.OK, twin.heartRateAvailability)
            }

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    @Test
    fun `refreshWellbeingSnapshot preserves permission sets but skips metric reads when health connect is unavailable`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.NotInstalled,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 9999L,
            avgHeartRateValue = 80.0
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                val snapshot = result.value
                assertEquals(HealthConnectUiState.NotInstalled, snapshot.healthConnectState)
                assertEquals(
                    setOf(stepsPermission, heartRatePermission),
                    snapshot.requiredPermissions
                )
                assertEquals(
                    setOf(stepsPermission, heartRatePermission),
                    snapshot.grantedPermissions
                )
                assertNull(snapshot.stepsPermissionGranted)
                assertNull(snapshot.heartRatePermissionGranted)

                val twin = snapshot.digitalTwinState
                assertEquals(DigitalTwinState.Availability.UNKNOWN, twin.stepsAvailability)
                assertEquals(DigitalTwinState.Availability.UNKNOWN, twin.heartRateAvailability)
                assertNull(twin.stepsLast24h)
                assertNull(twin.avgHeartRateLast24h)

                assertEquals(0, repo.readTotalStepsCalls)
                assertEquals(0, repo.readAvgHeartRateCalls)
            }

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    @Test
    fun `refreshWellbeingSnapshot degrades to empty granted permissions and denied metrics when granted lookup fails`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnGrantedPermissions = true
        )
        val orchestrator = newOrchestrator(repo)

        val result = runSuspend {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> {
                val snapshot = result.value
                assertEquals(HealthConnectUiState.Available, snapshot.healthConnectState)
                assertEquals(
                    setOf(stepsPermission, heartRatePermission),
                    snapshot.requiredPermissions
                )
                assertTrue(snapshot.grantedPermissions.isEmpty())
                assertEquals(false, snapshot.stepsPermissionGranted)
                assertEquals(false, snapshot.heartRatePermissionGranted)

                val twin = snapshot.digitalTwinState
                assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, twin.stepsAvailability)
                assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, twin.heartRateAvailability)
                assertNull(twin.stepsLast24h)
                assertNull(twin.avgHeartRateLast24h)

                assertEquals(0, repo.readTotalStepsCalls)
                assertEquals(0, repo.readAvgHeartRateCalls)
            }

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    @Test
    fun `bootstrap returns AUTH_REQUIRED when no authorized session exists`() {
        val identityRepo = FakeIdentityRepository()
        val orchestrator = newOrchestrator(identityRepository = identityRepo)

        val result = runSuspend { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success ->
                throw AssertionError("Expected Locked but got Success.")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Locked but got Error: ${result.message}")

            is LifeFlowOrchestrator.ActionResult.Locked -> {
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
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Authorized bootstrap creation test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository()
        val orchestrator = newOrchestrator(identityRepository = identityRepo)

        val result = runSuspend { orchestrator.bootstrapIdentityIfNeeded() }

        assertUnitSuccess(result)
        assertNotNull(identityRepo.activeIdentity)
        assertEquals(1, identityRepo.saveCalls)
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun `bootstrap keeps existing identity and does not save duplicate`() {
        forceResetSecurityState(
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
        val orchestrator = newOrchestrator(identityRepository = identityRepo)

        val result = runSuspend { orchestrator.bootstrapIdentityIfNeeded() }

        assertUnitSuccess(result)
        assertEquals(existing.id, identityRepo.activeIdentity?.id)
        assertEquals(0, identityRepo.saveCalls)
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun `bootstrap returns COMPROMISED lock when trust state is compromised`() {
        forceResetSecurityState(
            state = TrustState.COMPROMISED,
            reason = "Compromised bootstrap lock test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository()
        val orchestrator = newOrchestrator(identityRepository = identityRepo)

        val result = runSuspend { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success ->
                throw AssertionError("Expected Locked but got Success.")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Locked but got Error: ${result.message}")

            is LifeFlowOrchestrator.ActionResult.Locked -> {
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
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Bootstrap error path test"
        )
        SecurityAccessSession.grantDefault()

        val identityRepo = FakeIdentityRepository(throwsOnSave = true)
        val orchestrator = newOrchestrator(identityRepository = identityRepo)

        val result = runSuspend { orchestrator.bootstrapIdentityIfNeeded() }

        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success ->
                throw AssertionError("Expected Error but got Success.")

            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Error but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error -> {
                assertTrue(
                    "Expected bootstrap failure message.",
                    result.message.contains("save failed", ignoreCase = true)
                )
            }
        }

        assertNull(identityRepo.activeIdentity)
        assertEquals(1, identityRepo.saveCalls)
    }

    private fun newOrchestrator(
        wellbeingRepo: WellbeingRepository = defaultWellbeingRepository(),
        identityRepository: IdentityRepository = FakeIdentityRepository()
    ): LifeFlowOrchestrator {
        return LifeFlowOrchestrator(
            identityRepository = identityRepository,
            digitalTwinOrchestrator = DigitalTwinOrchestrator(DigitalTwinEngine()),
            getHealthConnectStatus = GetHealthConnectStatusUseCase(wellbeingRepo),
            getHealthPermissions = GetHealthPermissionsUseCase(wellbeingRepo),
            getGrantedHealthPermissions = GetGrantedHealthPermissionsUseCase(wellbeingRepo),
            getStepsLast24h = GetStepsLast24hUseCase(wellbeingRepo),
            getAvgHeartRateLast24h = GetAvgHeartRateLast24hUseCase(wellbeingRepo)
        )
    }

    private fun defaultWellbeingRepository(): WellbeingRepository {
        return FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0
        )
    }

    private fun assertSuccess(
        result: LifeFlowOrchestrator.ActionResult<DigitalTwinState>
    ): DigitalTwinState {
        return when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> result.value
            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    private fun assertUnitSuccess(
        result: LifeFlowOrchestrator.ActionResult<Unit>
    ) {
        when (result) {
            is LifeFlowOrchestrator.ActionResult.Success -> Unit
            is LifeFlowOrchestrator.ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is LifeFlowOrchestrator.ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    private fun forceResetSecurityState(
        state: TrustState,
        reason: String
    ) {
        val method = SecurityRuleEngine::class.java.declaredMethods.firstOrNull { candidate ->
            candidate.name.startsWith("forceResetForAdversarialSuite") &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0] == TrustState::class.java &&
                candidate.parameterTypes[1] == String::class.java
        } ?: throw AssertionError(
            buildString {
                append("Could not find compatible forceResetForAdversarialSuite method on SecurityRuleEngine. Available methods: ")
                append(SecurityRuleEngine::class.java.declaredMethods.joinToString { it.name })
            }
        )

        method.isAccessible = true
        method.invoke(SecurityRuleEngine, state, reason)
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var value: T? = null
        var failure: Throwable? = null

        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                result
                    .onSuccess { value = it }
                    .onFailure { failure = it }
            }
        })

        failure?.let { throw it }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    private class FakeIdentityRepository(
        initialActive: LifeFlowIdentity? = null,
        private val throwsOnGetActive: Boolean = false,
        private val throwsOnSave: Boolean = false
    ) : IdentityRepository {

        var activeIdentity: LifeFlowIdentity? = initialActive
            private set

        var saveCalls: Int = 0
            private set

        override suspend fun save(identity: LifeFlowIdentity) {
            saveCalls++
            if (throwsOnSave) {
                throw IllegalStateException("identity save failed")
            }
            activeIdentity = identity
        }

        override suspend fun getById(id: UUID): LifeFlowIdentity? {
            return activeIdentity?.takeIf { it.id == id }
        }

        override suspend fun getActiveIdentity(): LifeFlowIdentity? {
            if (throwsOnGetActive) {
                throw IllegalStateException("active identity lookup failed")
            }
            return activeIdentity
        }

        override suspend fun delete(identity: LifeFlowIdentity) {
            if (activeIdentity?.id == identity.id) {
                activeIdentity = null
            }
        }
    }

    private class FakeWellbeingRepository(
        private val sdkStatus: WellbeingRepository.SdkStatus,
        private val requiredPermissionsValue: Set<String>,
        private val grantedPermissionsValue: Set<String>,
        private val stepsValue: Long,
        private val avgHeartRateValue: Double?,
        private val throwsOnRequiredPermissions: Boolean = false,
        private val throwsOnGrantedPermissions: Boolean = false,
        private val throwsOnReadSteps: Boolean = false,
        private val throwsOnReadHeartRate: Boolean = false
    ) : WellbeingRepository {

        var requiredPermissionsCalls: Int = 0
            private set

        var grantedPermissionsCalls: Int = 0
            private set

        var readTotalStepsCalls: Int = 0
            private set

        var readAvgHeartRateCalls: Int = 0
            private set

        override fun getSdkStatus(): WellbeingRepository.SdkStatus = sdkStatus

        override fun requiredPermissions(): Set<String> {
            requiredPermissionsCalls++
            if (throwsOnRequiredPermissions) {
                throw IllegalStateException("required permissions unavailable")
            }
            return requiredPermissionsValue
        }

        override suspend fun grantedPermissions(): Set<String> {
            grantedPermissionsCalls++
            if (throwsOnGrantedPermissions) {
                throw IllegalStateException("granted permissions unavailable")
            }
            return grantedPermissionsValue
        }

        override suspend fun readTotalSteps(start: Instant, end: Instant): Long {
            readTotalStepsCalls++
            if (throwsOnReadSteps) {
                throw IllegalStateException("steps read failed")
            }
            return stepsValue
        }

        override suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double? {
            readAvgHeartRateCalls++
            if (throwsOnReadHeartRate) {
                throw IllegalStateException("heart-rate read failed")
            }
            return avgHeartRateValue
        }
    }
}