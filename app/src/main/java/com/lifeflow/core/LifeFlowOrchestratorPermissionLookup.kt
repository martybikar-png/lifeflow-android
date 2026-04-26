package com.lifeflow.core

import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import kotlinx.coroutines.CancellationException

internal fun lifeflowOrchestratorLookupRequiredHealthPermissions(
    getHealthPermissions: GetHealthPermissionsUseCase
): ActionResult<Set<String>> =
    try {
        ActionResult.Success(getHealthPermissions())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        ActionResult.Error("${exception::class.java.simpleName}: ${exception.message ?: "unknown error"}")
    }

internal suspend fun lifeflowOrchestratorLookupGrantedHealthPermissions(
    getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase
): ActionResult<Set<String>> =
    try {
        ActionResult.Success(getGrantedHealthPermissions())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        ActionResult.Success(emptySet())
    }
