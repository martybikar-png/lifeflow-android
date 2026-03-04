package com.lifeflow

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    private val repository: IdentityRepository,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase
) : ViewModel() {

    var uiState = mutableStateOf<UiState>(UiState.Loading)
        private set

    // ✅ Health Connect status (UI can display later)
    var healthConnectState = mutableStateOf<HealthConnectUiState>(HealthConnectUiState.Unknown)
        private set

    init {
        // Phase VI.B: fail-closed UI reaction to TrustState changes.
        viewModelScope.launch {
            SecurityRuleEngine.trustState.collect { state ->
                if (state == TrustState.COMPROMISED) {
                    SecurityAccessSession.clear()
                    uiState.value = UiState.Error(
                        "Security compromised. You can re-authenticate, or use 'Reset Vault' to recover (this wipes local encrypted data)."
                    )
                }
            }
        }
    }

    /**
     * Call from Activity after auth success (we keep it explicit).
     * This does NOT request permissions; it only detects if Health Connect is available.
     */
    fun refreshHealthConnectStatus() {
        val status = getHealthConnectStatus()
        healthConnectState.value = when (status) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
            else -> HealthConnectUiState.Unknown
        }
    }

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

sealed class HealthConnectUiState {
    object Unknown : HealthConnectUiState()
    object Available : HealthConnectUiState()
    object NotInstalled : HealthConnectUiState()
    object NotSupported : HealthConnectUiState()
    object UpdateRequired : HealthConnectUiState()
}