package com.lifeflow.domain.core.boundary

enum class BoundarySurfaceType {
    MODULE,
    ACTION,
    SCREEN,
    AUTOMATION
}

enum class BoundaryAccessOutcome {
    ALLOW,
    DENY,
    LOCK
}

data class BoundaryClassification(
    val key: String,
    val surfaceType: BoundarySurfaceType,
    val displayName: String,
    val requirement: TierRequirement,
    val auditName: String,
    val owner: String,
    val notes: String = ""
)
