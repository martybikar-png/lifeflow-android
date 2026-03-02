package com.lifeflow

sealed class UiState {
    object Loading : UiState()
    object Authenticated : UiState()
    data class Error(val message: String) : UiState()
}