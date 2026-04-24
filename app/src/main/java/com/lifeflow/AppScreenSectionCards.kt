package com.lifeflow

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
internal fun ActionCard(
    title: String,
    leadingIconResId: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LayoutSectionCard(
        title = title,
        leadingIconResId = leadingIconResId,
        content = content
    )
}

@Composable
private fun LayoutSectionCard(
    title: String,
    leadingIconResId: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LifeFlowCardShell(
        title = title,
        leadingIconResId = leadingIconResId,
        content = content
    )
}
