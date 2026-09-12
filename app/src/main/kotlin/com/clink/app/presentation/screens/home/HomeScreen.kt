package com.clink.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.domain.model.Pig
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkSectionHeader
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens

@Composable
fun HomeScreen(
    onNavigateToAddMoney: (pigId: Long) -> Unit,
    onNavigateToHistory: (pigId: Long) -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToPigDetail: (pigId: Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val primaryPigId = uiState.pigs.firstOrNull()?.id ?: 1L

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "CLINK 🐷",
                actions = {
                    IconButton(
                        onClick = onNavigateToGoals,
                        modifier = Modifier.size(ClinkDimens.current.minTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Goals",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToHistory(primaryPigId) },
                        modifier = Modifier.size(ClinkDimens.current.minTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Saving History",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddMoney(primaryPigId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = ClinkDimens.current.elevationLevel2
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ClinkDimens.current.iconSm)
                )
                Spacer(modifier = Modifier.width(ClinkDimens.current.spacingSm))
                Text(
                    text = "Add Savings",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = ClinkDimens.current.spacingLg)
        ) {
            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Hero Total Savings & Pig Progression Card
            val primaryPig = uiState.primaryPig
            val progression = primaryPig?.progression

            ClinkCard(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = ClinkDimens.current.elevationLevel2
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ClinkDimens.current.spacingXl)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
                            ) {
                                Text(
                                    text = (primaryPig?.name ?: "PRIMARY PIG").uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = primaryPig?.state?.badgeLabel ?: "🐣 New",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                            MoneyDisplay(
                                money = uiState.totalBalance,
                                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                            Text(
                                text = primaryPig?.state?.description ?: "Save tiny. Build habits. Clink!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

                        // Mascot Illustration reflecting current PigState
                        ClinkPigIllustration(
                            size = 84.dp,
                            state = primaryPig?.state ?: com.clink.app.domain.model.PigState.NEW,
                            contentDescription = "Pig is ${primaryPig?.state?.displayName ?: "New"}"
                        )
                    }

                    // Pig Progression Tier Bar
                    if (progression != null) {
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { progression.progressToNextTier },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val nextMilestoneText = if (progression.nextThreshold != null) {
                                val targetName = when (progression.state) {
                                    com.clink.app.domain.model.PigState.NEW,
                                    com.clink.app.domain.model.PigState.GROWING -> "Healthy Pig"
                                    com.clink.app.domain.model.PigState.HEALTHY -> "Full Pig"
                                    com.clink.app.domain.model.PigState.FULL -> "Max Tier"
                                }
                                "${(progression.progressToNextTier * 100).toInt()}% • Next: ${progression.nextThreshold.formatDisplay()} ($targetName)"
                            } else {
                                "🏆 Max Tier Achieved!"
                            }

                            Text(
                                text = nextMilestoneText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${uiState.totalBalance.formatDisplay()} saved",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            // Section Header
            ClinkSectionHeader(
                title = "Your Piggy Bank",
                subtitle = "Tap to view details and progression"
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            if (uiState.pigs.isEmpty()) {
                ClinkEmptyState(
                    title = "No piggy bank yet",
                    description = "Make your first clink to start your savings habit!",
                    actionButtonText = "Save First ₹10",
                    onActionClick = { onNavigateToAddMoney(primaryPigId) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.pigs, key = { it.id }) { pig ->
                        PigListItem(
                            pig = pig,
                            onClick = { onNavigateToPigDetail(pig.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxxl))
                    }
                }
            }
        }
    }
}

@Composable
fun PigListItem(
    pig: Pig,
    onClick: () -> Unit
) {
    ClinkCard(
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = ClinkDimens.current.elevationLevel1,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClinkDimens.current.spacingLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                ClinkPigIllustration(
                    size = 42.dp,
                    state = pig.state
                )
            }

            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pig.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = pig.state.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to view progression",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MoneyDisplay(
                money = pig.balance,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingSm))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(ClinkDimens.current.iconSm)
            )
        }
    }
}
