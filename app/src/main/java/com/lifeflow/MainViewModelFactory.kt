package com.lifeflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifeflow.domain.core.IdentityRepository

class MainViewModelFactory(
    private val repository: IdentityRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}