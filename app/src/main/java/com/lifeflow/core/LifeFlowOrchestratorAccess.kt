package com.lifeflow.core

import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.security.SecurityAuthorizationGate
import kotlinx.coroutines.CancellationException

internal enum class LifeFlowOrchestratorAuthContextKind {
    STANDARD_PROTECTED_CURRENT_SESSION,
    STRICT_PROTECTED_CURRENT_SESSION,
    TRUSTED_BASE_READ_ONLY_CURRENT_SESSION,
    BIOMETRIC_BOOTSTRAP_CURRENT_SESSION
}

internal fun lifeflowOrchestratorAuthorizeOperation(
    operation: DomainOperation,
    detail: String,
    contextKind: LifeFlowOrchestratorAuthContextKind
): ActionResult.Locked? {
    val lockedReason = when (contextKind) {
        LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION ->
            SecurityAuthorizationGate.standardProtectedLockedReasonOrNull(
                operation = operation,
                detail = detail
            )

        LifeFlowOrchestratorAuthContextKind.STRICT_PROTECTED_CURRENT_SESSION ->
            SecurityAuthorizationGate.strictProtectedLockedReasonOrNull(
                operation = operation,
                detail = detail
            )

        LifeFlowOrchestratorAuthContextKind.TRUSTED_BASE_READ_ONLY_CURRENT_SESSION ->
            SecurityAuthorizationGate.trustedBaseReadOnlyLockedReasonOrNull(
                operation = operation,
                detail = detail
            )

        LifeFlowOrchestratorAuthContextKind.BIOMETRIC_BOOTSTRAP_CURRENT_SESSION ->
            SecurityAuthorizationGate.biometricBootstrapLockedReasonOrNull(
                operation = operation,
                detail = detail
            )
    }

    return lockedReason?.let { ActionResult.Locked(it) }
}

internal suspend fun <T> lifeflowOrchestratorRunAccessControlledOperation(
    operation: DomainOperation,
    detail: String,
    contextKind: LifeFlowOrchestratorAuthContextKind,
    block: suspend () -> ActionResult<T>
): ActionResult<T> {
    lifeflowOrchestratorAuthorizeOperation(
        operation = operation,
        detail = detail,
        contextKind = contextKind
    )?.let { return it }

    return block()
}

internal suspend fun <T> lifeflowOrchestratorRunAccessControlledCatchingOperation(
    operation: DomainOperation,
    detail: String,
    contextKind: LifeFlowOrchestratorAuthContextKind,
    defaultErrorMessage: String,
    block: suspend () -> T
): ActionResult<T> {
    return lifeflowOrchestratorRunAccessControlledOperation(
        operation = operation,
        detail = detail,
        contextKind = contextKind
    ) {
        try {
            ActionResult.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            ActionResult.Error(exception.message ?: defaultErrorMessage)
        }
    }
}
