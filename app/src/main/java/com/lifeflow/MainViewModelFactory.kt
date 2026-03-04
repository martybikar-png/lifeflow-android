package com.lifeflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase

class MainViewModelFactory(
    private val repository: IdentityRepository,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            repository = repository,
            getHealthConnectStatus = getHealthConnectStatus
        ) as T
    }
}