package com.lifeflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.security.TrustStatePort

class MainViewModelFactory(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit,
    private val isSessionAuthorized: () -> Boolean,
    private val clearSession: () -> Unit,
    private val trustStatePort: TrustStatePort
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                orchestrator = orchestrator,
                performVaultReset = performVaultReset,
                trustStatePort = trustStatePort,
                isSessionAuthorized = isSessionAuthorized,
                clearSession = clearSession
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
