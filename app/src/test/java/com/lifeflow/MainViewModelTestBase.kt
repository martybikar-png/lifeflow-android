@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainViewModelTestBase {

    protected lateinit var testDispatcher: TestCoroutineDispatcher

    protected val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
    protected val heartRatePermission = HealthPermission.getReadPermission(HeartRateRecord::class)

    @Before
    fun setUpBase() {
        testDispatcher = TestCoroutineDispatcher()
        Dispatchers.setMain(testDispatcher)
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "MainViewModelTest baseline reset"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    @After
    fun tearDownBase() {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    protected fun runMain() {
        drainMainQueue()
    }

    protected fun advanceMainTimeBy(milliseconds: Long) {
        testDispatcher.advanceTimeBy(milliseconds)
        drainMainQueue()
    }

    private fun drainMainQueue() {
        repeat(8) {
            testDispatcher.runCurrent()
            testDispatcher.advanceTimeBy(1)
        }
        testDispatcher.runCurrent()
    }

    protected fun newViewModel(
        wellbeingRepo: WellbeingRepository = defaultWellbeingRepository(),
        identityRepository: IdentityRepository = FakeIdentityRepository(),
        performVaultReset: suspend () -> Unit = {}
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
            performVaultReset = performVaultReset
        )
    }

    protected fun defaultWellbeingRepository(): WellbeingRepository {
        return FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = emptySet(),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0
        )
    }

    protected fun clearViewModel(viewModel: MainViewModel) {
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

    protected fun forceResetSecurityState(
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
}

internal class FakeIdentityRepository(
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

internal class FakeWellbeingRepository(
    var sdkStatusValue: WellbeingRepository.SdkStatus,
    var requiredPermissionsValue: Set<String>,
    var grantedPermissionsValue: Set<String>,
    var stepsValue: Long,
    var avgHeartRateValue: Double?,
    var throwsOnRequiredPermissions: Boolean = false,
    var throwsOnGrantedPermissions: Boolean = false,
    var throwsOnReadSteps: Boolean = false,
    var throwsOnReadHeartRate: Boolean = false
) : WellbeingRepository {

    override fun getSdkStatus(): WellbeingRepository.SdkStatus = sdkStatusValue

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