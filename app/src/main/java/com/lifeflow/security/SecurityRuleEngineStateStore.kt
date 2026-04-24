package com.lifeflow.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SecurityRuleEngineStateStore(
    initialState: TrustState
) {
    private val mutableTrustState = MutableStateFlow(initialState)

    val trustState: StateFlow<TrustState> =
        mutableTrustState.asStateFlow()

    fun get(): TrustState =
        mutableTrustState.value

    fun set(state: TrustState) {
        mutableTrustState.value = state
    }
}