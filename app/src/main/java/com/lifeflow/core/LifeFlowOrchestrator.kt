package com.lifeflow.core

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.lifeflow.domain.core.IdentityRepository
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
import java.util.UUID
import kotlin.math.roundToLong

/**
 * Phase A — Orchestration / Authority Expansion
 *
 * Single entrypoint for sensitive operations:
 * - identity bootstrap
 * - consent + HC status
 * - wellbeing reads
 * - digital twin refresh
 *
 * Zero-bypass principle: UI/ViewModel should call only this orchestrator
 * for sensitive work. Fail-closed for security gating.
 */
class LifeFlowOrchestrator(
    private val identityRepository: IdentityRepository,
    private val digitalTwinOrchestrator: DigitalTwinOrchestrator,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    private val getStepsLast24h: GetStepsLast24hUseCase,
    private val getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase
) {

    private data class MetricPermissionSnapshot(
        val stepsPermissionGranted: Boolean?,
        val heartRatePermissionGranted: Boolean?
    )

    // ---------- Result model (deterministic, UI-friendly) ----------

    sealed class ActionResult<out T> {
        data class Success<T>(val value: T) : ActionResult<T>()
        data class Locked(val reason: String) : ActionResult<Nothing>()
        data class Error(val message: String) : ActionResult<Nothing>()
    }

    // ---------- Health Connect status ----------

    fun healthConnectUiState(): HealthConnectUiState {
        return when (getHealthConnectStatus()) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
        }
    }

    fun requiredHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getHealthPermissions())
        } catch (t: Throwable) {
            ActionResult.Error("${t::class.java.simpleName}: ${t.message ?: "unknown error"}")
        }

    suspend fun grantedHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getGrantedHealthPermissions())
        } catch (_: Throwable) {
            // Permissions read should never break the app; return empty set deterministically
            ActionResult.Success(emptySet())
        }

    // ---------- Security / Identity ----------

    /**
     * Hard gate for sensitive operations.
     * Fail-closed:
     * - no session => locked
     * - compromised trust => locked
     */
    @Suppress("SameParameterValue")
    private fun gateOrLocked(reason: String): ActionResult.Locked? {
        if (SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
            return ActionResult.Locked("COMPROMISED: $reason")
        }
        if (!SecurityAccessSession.isAuthorized()) {
            return ActionResult.Locked("AUTH_REQUIRED: $reason")
        }
        return null
    }

    suspend fun bootstrapIdentityIfNeeded(): ActionResult<Unit> {
        gateOrLocked("Identity bootstrap")?.let { return it }

        return try {
            SecurityRuleEngine.setTrustState(
                TrustState.VERIFIED,
                reason = "Auth session active (bootstrapIdentityIfNeeded)"
            )

            val active = identityRepository.getActiveIdentity()
            if (active == null) {
                val newIdentity = LifeFlowIdentity(
                    id = UUID.randomUUID(),
                    createdAtEpochMillis = System.currentTimeMillis(),
                    isActive = true
                )
                identityRepository.save(newIdentity)
            }

            ActionResult.Success(Unit)
        } catch (e: SecurityException) {
            SecurityAccessSession.clear()
            ActionResult.Locked(e.message ?: "Security denied")
        } catch (t: Throwable) {
            SecurityAccessSession.clear()
            ActionResult.Error(t.message ?: "Bootstrap failed")
        }
    }

    // ---------- Metrics + Digital Twin ----------

    /**
     * Phase 2:
     * Resolve permission status per metric instead of one bundled flag.
     *
     * Semantics:
     * - true  -> permission resolved and granted
     * - false -> permission resolved and not granted
     * - null  -> permission state unknown / not resolvable right now
     */
    private suspend fun resolveMetricPermissionSnapshot(): MetricPermissionSnapshot {
        return try {
            val requiredPermissions = getHealthPermissions()
            if (requiredPermissions.isEmpty()) {
                return MetricPermissionSnapshot(
                    stepsPermissionGranted = null,
                    heartRatePermissionGranted = null
                )
            }

            val grantedPermissions = getGrantedHealthPermissions()

            val stepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
            val heartRatePermission = HealthPermission.getReadPermission(HeartRateRecord::class)

            val stepsRequired = requiredPermissions.contains(stepsPermission)
            val heartRateRequired = requiredPermissions.contains(heartRatePermission)

            val stepsGranted = if (stepsRequired) {
                grantedPermissions.contains(stepsPermission)
            } else {
                null
            }

            val heartRateGranted = if (heartRateRequired) {
                grantedPermissions.contains(heartRatePermission)
            } else {
                null
            }

            MetricPermissionSnapshot(
                stepsPermissionGranted = stepsGranted,
                heartRatePermissionGranted = heartRateGranted
            )
        } catch (_: Throwable) {
            MetricPermissionSnapshot(
                stepsPermissionGranted = null,
                heartRatePermissionGranted = null
            )
        }
    }

    /**
     * Best-effort metrics read is NOT security-sensitive by itself,
     * but it MUST still obey consent boundary (HC availability + permissions).
     *
     * identityInitialized tells the twin whether identity core is ready.
     */
    suspend fun refreshTwinBestEffort(identityInitialized: Boolean): ActionResult<DigitalTwinState> {
        val hc = healthConnectUiState()
        if (hc !is HealthConnectUiState.Available) {
            val state = digitalTwinOrchestrator.refresh(
                identityInitialized = identityInitialized,
                stepsLast24h = null,
                avgHeartRateLast24h = null,
                stepsPermissionGranted = null,
                heartRatePermissionGranted = null
            )
            return ActionResult.Success(state)
        }

        val permissionSnapshot = resolveMetricPermissionSnapshot()

        var steps: Long? = null
        var hr: Long? = null

        if (permissionSnapshot.stepsPermissionGranted == true) {
            try {
                steps = getStepsLast24h()
            } catch (_: Throwable) {
                // keep null; engine will classify deterministically
            }
        }

        if (permissionSnapshot.heartRatePermissionGranted == true) {
            try {
                hr = getAvgHeartRateLast24h()?.roundToLong()
            } catch (_: Throwable) {
                // keep null; engine will classify deterministically
            }
        }

        val state = digitalTwinOrchestrator.refresh(
            identityInitialized = identityInitialized,
            stepsLast24h = steps,
            avgHeartRateLast24h = hr,
            stepsPermissionGranted = permissionSnapshot.stepsPermissionGranted,
            heartRatePermissionGranted = permissionSnapshot.heartRatePermissionGranted
        )

        return ActionResult.Success(state)
    }
}

/**
 * Local UI state for Health Connect.
 * Keep it stable and explicit (no hidden magic).
 */
sealed class HealthConnectUiState {
    object Unknown : HealthConnectUiState()
    object Available : HealthConnectUiState()
    object NotInstalled : HealthConnectUiState()
    object NotSupported : HealthConnectUiState()
    object UpdateRequired : HealthConnectUiState()
}