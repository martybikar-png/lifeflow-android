package com.lifeflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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

private val HomeCardShape = RoundedCornerShape(30.dp)
private val HomeActionShape = RoundedCornerShape(20.dp)
private val HomeCardPadding = 24.dp
private val HomeCardSpacing = 16.dp

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
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lf_ic_focus),
                        contentDescription = "Home focus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "LifeFlow home",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "One calm step first",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Text(
                text = "Home should feel quiet, clear, and premium. The first move stays obvious, and the rest stays soft.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Quick Capture stays first. Settings and Trust remain available, but secondary.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
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
                    text = "Quick Capture",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Add one small thing without turning Home into a dense dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpenQuickCapture,
                modifier = Modifier.fillMaxWidth(),
                shape = HomeActionShape
            ) {
                Text("Open Quick Capture")
            }

            Text(
                text = "Best for a note, signal, mood check, or short reflection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Settings and Trust stay close, but visually quieter than the main action.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                    shape = HomeActionShape
                ) {
                    Text("Settings")
                }

                OutlinedButton(
                    onClick = onOpenTrust,
                    modifier = Modifier.weight(1f),
                    shape = HomeActionShape
                ) {
                    Text("Trust")
                }
            }
        }
    }
}

@Composable
private fun HomeBoundaryCard() {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeCardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current boundary",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "This Home screen is still a shell. It shapes structure and hierarchy only — not biometric truth, trust-state branching, recovery, or protected execution.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

