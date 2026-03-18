package com.lifeflow.domain.wellbeing.usecase

import com.lifeflow.domain.wellbeing.WellbeingRepository

class GetGrantedHealthPermissionsUseCase(
    private val repo: WellbeingRepository
) {
    suspend operator fun invoke(): Set<String> = repo.grantedPermissions()
}