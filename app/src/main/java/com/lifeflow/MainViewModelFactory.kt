package com.lifeflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.core.LifeFlowOrchestrator

class MainViewModelFactory(
    private val orchestrator: LifeFlowOrchestrator,
    private val performVaultReset: suspend () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                orchestrator = orchestrator,
                performVaultReset = performVaultReset
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}