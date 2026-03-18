package com.lifeflow

import com.lifeflow.domain.core.TierState

sealed class UiState {
    object Loading : UiState()
    object Authenticated : UiState()
    data class FreeTier(
        val message: String = "You are using LifeFlow Free. Upgrade to Core to unlock all features."
    ) : UiState()
    data class Error(val message: String) : UiState()
}
