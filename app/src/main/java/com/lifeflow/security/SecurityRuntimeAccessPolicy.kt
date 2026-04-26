package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal object SecurityRuntimeAccessPolicy {
    private val snapshotProvider = SecurityRuntimeAccessSnapshotProvider()
    private val entryEvaluator = SecurityRuntimeEntryAccessEvaluator(snapshotProvider)
    private val authorizationEvaluator = SecurityRuntimeAuthorizationEvaluator(snapshotProvider)
    private val operationEvaluator = SecurityRuntimeOperationEvaluator(snapshotProvider)

    fun decideStandardProtectedEntry(): SecurityRuntimeAccessDecision =
        entryEvaluator.decideStandardProtectedEntry()

    fun decideTrustedBaseReadEntry(): SecurityRuntimeAccessDecision =
        entryEvaluator.decideTrustedBaseReadEntry()

    fun decideBiometricAuthenticationHandoff(): SecurityRuntimeAccessDecision =
        entryEvaluator.decideBiometricAuthenticationHandoff()

    fun decideBiometricBootstrapOperation(
        operation: DomainOperation
    ): SecurityRuntimeAccessDecision =
        entryEvaluator.decideBiometricBootstrapOperation(operation)

    fun decideAuthorization(
        request: SecurityRuntimeAuthorizationRequest
    ): SecurityRuntimeAuthorizationDecision =
        authorizationEvaluator.decideAuthorization(request)

    fun decideOperation(
        operation: DomainOperation
    ): SecurityRuntimeOperationDecision =
        operationEvaluator.decideOperation(operation)
}
