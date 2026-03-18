package com.lifeflow.core

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
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
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

internal val testStepsPermission: String =
    HealthPermission.getReadPermission(StepsRecord::class)

internal val testHeartRatePermission: String =
    HealthPermission.getReadPermission(HeartRateRecord::class)

internal fun resetSecurityBaselineForLifeFlowOrchestratorTests() {
    forceResetSecurityStateForLifeFlowOrchestratorTests(
        state = TrustState.DEGRADED,
        reason = "LifeFlowOrchestratorTest baseline reset"
    )
    SecurityAccessSession.clear()
    SecurityRuleEngine.clearAudit()
}

internal fun newTestLifeFlowOrchestrator(
    wellbeingRepo: WellbeingRepository = defaultTestWellbeingRepository(),
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

internal fun defaultTestWellbeingRepository(): WellbeingRepository {
    return FakeWellbeingRepository(
        sdkStatus = WellbeingRepository.SdkStatus.Available,
        requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
        grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
        stepsValue = 1000L,
        avgHeartRateValue = 70.0
    )
}

internal fun assertWellbeingSnapshotSuccess(
    result: ActionResult<WellbeingRefreshSnapshot>
): WellbeingRefreshSnapshot {
    return when (result) {
        is ActionResult.Success -> result.value
        is ActionResult.Locked ->
            throw AssertionError("Expected Success but got Locked: ${result.reason}")

        is ActionResult.Error ->
            throw AssertionError("Expected Success but got Error: ${result.message}")
    }
}

internal fun assertUnitSuccess(
    result: ActionResult<Unit>
) {
    when (result) {
        is ActionResult.Success -> Unit
        is ActionResult.Locked ->
            throw AssertionError("Expected Success but got Locked: ${result.reason}")

        is ActionResult.Error ->
            throw AssertionError("Expected Success but got Error: ${result.message}")
    }
}

internal fun forceResetSecurityStateForLifeFlowOrchestratorTests(
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

internal fun <T> runSuspendTest(block: suspend () -> T): T {
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