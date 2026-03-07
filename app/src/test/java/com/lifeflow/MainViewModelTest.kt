package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.core.LifeFlowOrchestrator
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
import com.lifeflow.security.ResetVaultUseCase
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
    private val heartRatePermission = HealthPermission.getReadPermission(HeartRateRecord::class)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "MainViewModelTest baseline reset"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    @After
    fun tearDown() {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads required permissions and keeps init error null`() {
        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = emptySet(),
                stepsValue = 0L,
                avgHeartRateValue = null
            )
        )

        try {
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertNull(viewModel.healthPermissionsInitError.value)
            assertTrue(viewModel.uiState.value is UiState.Loading)
        } finally {
            clearViewModel(viewModel)
        }
    }

    @Test
    fun `init stores permissions init error when required permissions lookup fails`() {
        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = emptySet(),
                stepsValue = 0L,
                avgHeartRateValue = null,
                throwsOnRequiredPermissions = true
            )
        )

        try {
            assertTrue(viewModel.requiredHealthPermissions.value.isEmpty())
            assertNotNull(viewModel.healthPermissionsInitError.value)
            assertTrue(
                viewModel.healthPermissionsInitError.value!!.contains("IllegalStateException")
            )
        } finally {
            clearViewModel(viewModel)
        }
    }

    @Test
    fun `onAuthenticationSuccess returns error when auth session is missing`() = runTest {
        val viewModel = newViewModel()

        try {
            viewModel.onAuthenticationSuccess()
            runCurrent()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Active auth session missing"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            runCurrent()
        }
    }

    @Test
    fun `onAuthenticationSuccess moves to authenticated when session exists`() = runTest {
        SecurityAccessSession.grantDefault()

        val viewModel = newViewModel()

        try {
            viewModel.onAuthenticationSuccess()
            runCurrent()

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
        } finally {
            clearViewModel(viewModel)
            runCurrent()
        }
    }

    @Test
    fun `refreshMetricsAndTwinNow updates health state permissions and digital twin after auth`() = runTest {
        SecurityAccessSession.grantDefault()

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 4321L,
                avgHeartRateValue = 71.6
            )
        )

        try {
            viewModel.onAuthenticationSuccess()
            runCurrent()

            viewModel.refreshMetricsAndTwinNow()
            runCurrent()

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertEquals(HealthConnectUiState.Available, viewModel.healthConnectState.value)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.grantedHealthPermissions.value
            )

            val twin = viewModel.digitalTwinState.value
            assertNotNull(twin)
            assertTrue(twin!!.identityInitialized)
            assertEquals(4321L, twin.stepsLast24h)
            assertEquals(72L, twin.avgHeartRateLast24h)
            assertEquals(DigitalTwinState.Availability.OK, twin.stepsAvailability)
            assertEquals(DigitalTwinState.Availability.OK, twin.heartRateAvailability)
        } finally {
            clearViewModel(viewModel)
            runCurrent()
        }
    }

    @Test
    fun `session expiry wipes caches and moves ui to error`() = runTest {
        SecurityAccessSession.grantDefault()

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 1234L,
                avgHeartRateValue = 65.0
            )
        )

        try {
            viewModel.onAuthenticationSuccess()
            runCurrent()

            viewModel.refreshMetricsAndTwinNow()
            runCurrent()

            assertNotNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            SecurityAccessSession.clear()
            advanceTimeBy(1000)
            runCurrent()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Session expired"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            runCurrent()
        }
    }

    @Test
    fun `onAuthenticationError clears session wipes caches and exposes error`() = runTest {
        SecurityAccessSession.grantDefault()

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 2222L,
                avgHeartRateValue = 70.0
            )
        )

        try {
            viewModel.onAuthenticationSuccess()
            runCurrent()

            viewModel.refreshMetricsAndTwinNow()
            runCurrent()

            viewModel.onAuthenticationError("Biometric authentication failed")

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Biometric authentication failed"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runCurrent()
        }
    }

    private fun newViewModel(
        wellbeingRepo: WellbeingRepository = defaultWellbeingRepository(),
        identityRepository: IdentityRepository = FakeIdentityRepository()
    ): MainViewModel {
        val orchestrator = LifeFlowOrchestrator(
            identityRepository = identityRepository,
            digitalTwinOrchestrator = DigitalTwinOrchestrator(DigitalTwinEngine()),
            getHealthConnectStatus = GetHealthConnectStatusUseCase(wellbeingRepo),
            getHealthPermissions = GetHealthPermissionsUseCase(wellbeingRepo),
            getGrantedHealthPermissions = GetGrantedHealthPermissionsUseCase(wellbeingRepo),
            getStepsLast24h = GetStepsLast24hUseCase(wellbeingRepo),
            getAvgHeartRateLast24h = GetAvgHeartRateLast24hUseCase(wellbeingRepo)
        )

        return MainViewModel(
            orchestrator = orchestrator,
            resetVaultUseCase = allocateResetVaultUseCaseWithoutConstructor()
        )
    }

    private fun defaultWellbeingRepository(): WellbeingRepository {
        return FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = emptySet(),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun allocateResetVaultUseCaseWithoutConstructor(): ResetVaultUseCase {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val field = unsafeClass.getDeclaredField("theUnsafe")
        field.isAccessible = true
        val unsafe = field.get(null)
        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, ResetVaultUseCase::class.java) as ResetVaultUseCase
    }

    private fun clearViewModel(viewModel: MainViewModel) {
        val methods = ViewModel::class.java.declaredMethods.toList() + ViewModel::class.java.methods.toList()

        val clearMethod = methods.firstOrNull { candidate ->
            candidate.parameterCount == 0 && candidate.name.startsWith("clear")
        } ?: throw AssertionError(
            buildString {
                append("Could not find compatible ViewModel clear method. Available methods: ")
                append(
                    methods
                        .map { it.name }
                        .distinct()
                        .sorted()
                        .joinToString()
                )
            }
        )

        clearMethod.isAccessible = true
        clearMethod.invoke(viewModel)
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

        override fun getSdkStatus(): WellbeingRepository.SdkStatus = sdkStatus

        override fun requiredPermissions(): Set<String> {
            if (throwsOnRequiredPermissions) {
                throw IllegalStateException("required permissions unavailable")
            }
            return requiredPermissionsValue
        }

        override suspend fun grantedPermissions(): Set<String> {
            if (throwsOnGrantedPermissions) {
                throw IllegalStateException("granted permissions unavailable")
            }
            return grantedPermissionsValue
        }

        override suspend fun readTotalSteps(start: Instant, end: Instant): Long {
            if (throwsOnReadSteps) {
                throw IllegalStateException("steps read failed")
            }
            return stepsValue
        }

        override suspend fun readAvgHeartRateBpm(start: Instant, end: Instant): Double? {
            if (throwsOnReadHeartRate) {
                throw IllegalStateException("heart-rate read failed")
            }
            return avgHeartRateValue
        }
    }
}