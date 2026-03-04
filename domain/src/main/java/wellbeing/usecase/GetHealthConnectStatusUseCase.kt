package com.lifeflow.domain.wellbeing.usecase

import com.lifeflow.domain.wellbeing.WellbeingRepository

class GetHealthConnectStatusUseCase(
    private val repo: WellbeingRepository
) {
    operator fun invoke(): WellbeingRepository.SdkStatus = repo.getSdkStatus()
}