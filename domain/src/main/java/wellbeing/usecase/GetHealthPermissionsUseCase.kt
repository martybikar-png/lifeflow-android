package com.lifeflow.domain.wellbeing.usecase

import com.lifeflow.domain.wellbeing.WellbeingRepository

class GetHealthPermissionsUseCase(
    private val repo: WellbeingRepository
) {
    operator fun invoke(): Set<String> = repo.requiredPermissions()
}