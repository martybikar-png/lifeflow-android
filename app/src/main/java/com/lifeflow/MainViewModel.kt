package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.security.SecurityAccessSession
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    private val repository: IdentityRepository
) : ViewModel() {

    var uiState = mutableStateOf<UiState>(UiState.Loading)
        private set

    fun onAuthenticationSuccess() {
        uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val active = repository.getActiveIdentity()

                if (active == null) {
                    val newIdentity = LifeFlowIdentity(
                        id = UUID.randomUUID(),
                        createdAtEpochMillis = System.currentTimeMillis(),
                        isActive = true
                    )
                    repository.save(newIdentity)
                }

                uiState.value = UiState.Authenticated
            } catch (e: SecurityException) {
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(e.message ?: "Security denied")
            } catch (t: Throwable) {
                SecurityAccessSession.clear()
                uiState.value = UiState.Error(t.message ?: "Bootstrap failed")
            }
        }
    }

    fun onAuthenticationError(message: String) {
        SecurityAccessSession.clear()
        uiState.value = UiState.Error(message)
    }
}