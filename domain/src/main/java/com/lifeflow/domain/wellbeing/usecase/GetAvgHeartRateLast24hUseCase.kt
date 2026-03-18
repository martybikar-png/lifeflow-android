package com.lifeflow.domain.wellbeing.usecase

import com.lifeflow.domain.wellbeing.WellbeingRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetAvgHeartRateLast24hUseCase(
    private val repo: WellbeingRepository
) {
    suspend operator fun invoke(now: Instant = Instant.now()): Double? {
        val end = now
        val start = end.minus(24, ChronoUnit.HOURS)
        return repo.readAvgHeartRateBpm(start, end)
    }
}