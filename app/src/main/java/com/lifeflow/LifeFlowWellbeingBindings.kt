package com.lifeflow

import android.content.Context
import com.lifeflow.data.wellbeing.HealthConnectWellbeingRepository
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase

internal class LifeFlowWellbeingBindings private constructor(
    val wellbeingRepository: WellbeingRepository,
    val getHealthConnectStatusUseCase: GetHealthConnectStatusUseCase,
    val getHealthPermissionsUseCase: GetHealthPermissionsUseCase,
    val getGrantedHealthPermissionsUseCase: GetGrantedHealthPermissionsUseCase,
    val getStepsLast24hUseCase: GetStepsLast24hUseCase,
    val getAvgHeartRateLast24hUseCase: GetAvgHeartRateLast24hUseCase
) {
    companion object {
        fun create(
            applicationContext: Context
        ): LifeFlowWellbeingBindings {
            val wellbeingRepository = HealthConnectWellbeingRepository(applicationContext)

            return LifeFlowWellbeingBindings(
                wellbeingRepository = wellbeingRepository,
                getHealthConnectStatusUseCase = GetHealthConnectStatusUseCase(wellbeingRepository),
                getHealthPermissionsUseCase = GetHealthPermissionsUseCase(wellbeingRepository),
                getGrantedHealthPermissionsUseCase =
                    GetGrantedHealthPermissionsUseCase(wellbeingRepository),
                getStepsLast24hUseCase = GetStepsLast24hUseCase(wellbeingRepository),
                getAvgHeartRateLast24hUseCase =
                    GetAvgHeartRateLast24hUseCase(wellbeingRepository)
            )
        }
    }
}
