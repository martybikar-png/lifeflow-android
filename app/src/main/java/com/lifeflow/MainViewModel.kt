package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.lifeflow.domain.core.IdentityRepository

class MainViewModel(
    private val repository: IdentityRepository
) : ViewModel() {

    var uiState = mutableStateOf<UiState>(UiState.Loading)
        private set

    fun onAuthenticationSuccess() {
        // tady později načteme identity z repository
        uiState.value = UiState.Authenticated
    }

    fun onAuthenticationError(message: String) {
        uiState.value = UiState.Error(message)
    }
}