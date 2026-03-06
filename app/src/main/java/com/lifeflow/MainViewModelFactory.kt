package com.lifeflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.security.ResetVaultUseCase

class MainViewModelFactory(
    private val orchestrator: LifeFlowOrchestrator,
    private val resetVaultUseCase: ResetVaultUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                orchestrator = orchestrator,
                resetVaultUseCase = resetVaultUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}