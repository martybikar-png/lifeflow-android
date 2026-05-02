package com.lifeflow.security

import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SecurityRuleEngineStateStore(
    initialState: TrustState
) {
    private val mutableTrustState = MutableStateFlow(initialState)
    private val mutableLastTransitionAt = MutableStateFlow(Instant.now())

    val trustState: StateFlow<TrustState> =
        mutableTrustState.asStateFlow()

    fun get(): TrustState =
        mutableTrustState.value

    fun lastTransitionAt(): Instant =
        mutableLastTransitionAt.value

    fun set(state: TrustState) {
        mutableTrustState.value = state
        mutableLastTransitionAt.value = Instant.now()
    }
}
