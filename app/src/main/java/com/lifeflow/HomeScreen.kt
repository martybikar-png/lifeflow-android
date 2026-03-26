package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val HomeCardShape = RoundedCornerShape(28.dp)
private val HomeInnerShape = RoundedCornerShape(22.dp)
private val HomePillShape = RoundedCornerShape(18.dp)
private val HomeCardPadding = 24.dp
private val HomeCardSpacing = 18.dp

@Composable
fun HomeScreen(
    lastAction: String = "Home shell active",
    onOpenQuickCapture: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTrust: () -> Unit = {},
    debugLines: List<String> = emptyList()
) {
    ScreenContainer(title = "Home") {
        HomeHeroCard()
        ScreenSectionSpacer()
        HomePrimaryFocusCard(onOpenQuickCapture = onOpenQuickCapture)
        ScreenSectionSpacer()
        HomeSupportingSurfacesCard(
            onOpenSettings = onOpenSettings,
            onOpenTrust = onOpenTrust
        )
        ScreenSectionSpacer()
        HomeBoundaryCard()
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
private fun HomeHeroCard() {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(HomeCardSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = HomeInnerShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lf_ic_focus),
                        contentDescription = "Home focus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quiet home",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "One clear step first",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Text(
                text = "Home is the calm center of LifeFlow. It should feel soft, focused, and premium — one meaningful action first, then only a small amount of supporting context.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomePill(text = "Focus-first")
                HomePill(text = "Calm shell")
                HomePill(text = "Soft direction")
            }
        }
    }
}

@Composable
private fun HomePrimaryFocusCard(
    onOpenQuickCapture: () -> Unit
) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(HomeCardSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Primary focus",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Open Quick Capture to add one small thing without turning the home into a busy dashboard. This keeps the first step light, fast, and emotionally quiet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = HomeInnerShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Capture something small",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Quick note, mood reflection, signal, or lightweight prompt — one clean action before the rest of the system gets involved.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onOpenQuickCapture,
                modifier = Modifier.fillMaxWidth(),
                shape = HomeInnerShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Open Quick Capture")
            }
        }
    }
}

@Composable
private fun HomeSupportingSurfacesCard(
    onOpenSettings: () -> Unit,
    onOpenTrust: () -> Unit
) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(HomeCardSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Supporting surfaces",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Settings and Trust stay secondary here. They should remain accessible, but visually softer than the primary capture path.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = HomeInnerShape
            ) {
                Text("Open Settings")
            }

            OutlinedButton(
                onClick = onOpenTrust,
                modifier = Modifier.fillMaxWidth(),
                shape = HomeInnerShape
            ) {
                Text("Open Trust")
            }
        }
    }
}

@Composable
private fun HomeBoundaryCard() {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Current boundary",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "This is still a shell-oriented home. It does not decide biometric truth, trust-state branching, recovery behavior, or protected execution. The goal here is calm structure and visual direction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomePill(text: String) {
    Surface(
        shape = HomePillShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
