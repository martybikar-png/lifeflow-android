@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.lifeflow

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import com.lifeflow.core.FakeIdentityRepository
import com.lifeflow.core.newTestLifeFlowOrchestrator
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.SecurityTrustStatePortAdapter
import com.lifeflow.security.TrustState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

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

    protected fun settleMain() {
        runMain()
        runMain()
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
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = wellbeingRepo,
            identityRepository = identityRepository
        )

        return MainViewModel(
            orchestrator = orchestrator,
            performVaultReset = performVaultReset,
            trustStatePort = SecurityTrustStatePortAdapter(),
            isSessionAuthorized = { SecurityAccessSession.isAuthorized() },
            clearSession = { SecurityAccessSession.clear() }
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
