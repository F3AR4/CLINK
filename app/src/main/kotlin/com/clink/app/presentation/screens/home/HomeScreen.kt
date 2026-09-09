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

            // Hero Total Savings Card
            ClinkCard(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = ClinkDimens.current.elevationLevel2
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ClinkDimens.current.spacingXl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL SAVED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                        MoneyDisplay(
                            money = uiState.totalBalance,
                            fontSize = MaterialTheme.typography.displayMedium.fontSize,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                        Text(
                            text = "Save tiny. Build habits. Clink!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

                    // Mascot Illustration
                    ClinkPigIllustration(
                        size = 76.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            // Section Header
            ClinkSectionHeader(
                title = "Your Piggy Banks",
                subtitle = "Where your micro-savings live"
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
                ClinkPigIllustration(size = 40.dp)
            }

            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pig.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to view details",
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
