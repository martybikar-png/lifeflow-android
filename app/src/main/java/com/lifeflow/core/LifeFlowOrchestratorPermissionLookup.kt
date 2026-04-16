package com.lifeflow.core

import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase

internal fun lifeflowOrchestratorLookupRequiredHealthPermissions(
    getHealthPermissions: GetHealthPermissionsUseCase
): ActionResult<Set<String>> =
    try {
        ActionResult.Success(getHealthPermissions())
    } catch (t: Throwable) {
        ActionResult.Error("${t::class.java.simpleName}: ${t.message ?: "unknown error"}")
    }

internal suspend fun lifeflowOrchestratorLookupGrantedHealthPermissions(
    getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase
): ActionResult<Set<String>> =
    try {
        ActionResult.Success(getGrantedHealthPermissions())
    } catch (_: Throwable) {
        ActionResult.Success(emptySet())
    }
