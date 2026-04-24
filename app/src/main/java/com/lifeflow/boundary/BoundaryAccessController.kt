package com.lifeflow.boundary

import com.lifeflow.domain.core.boundary.BoundaryDecision
import com.lifeflow.domain.core.boundary.BoundaryEvaluator
import com.lifeflow.domain.core.boundary.EntitlementState

class BoundaryAccessController(
    private val evaluator: BoundaryEvaluator = BoundaryEvaluator(),
    private val presentationResolver: BoundaryPresentationResolver = BoundaryPresentationResolver()
) {
    fun evaluate(
        boundaryKey: String,
        entitlementState: EntitlementState
    ): BoundaryAccessBundle {
        val decision = evaluator.evaluate(
            boundaryKey = boundaryKey,
            entitlementState = entitlementState
        )

        val presentation = presentationResolver.resolve(decision)

        return BoundaryAccessBundle(
            decision = decision,
            presentation = presentation
        )
    }
}

data class BoundaryAccessBundle(
    val decision: BoundaryDecision,
    val presentation: BoundaryPresentation
)
