package com.clink.app.presentation.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.model.Transaction
import com.clink.app.presentation.components.AnimatedMoneyDisplay
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
import com.clink.app.presentation.components.ClinkOutlinedButton
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkSectionHeader
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToAddMoney: (pigId: Long) -> Unit,
    onNavigateToHistory: (pigId: Long) -> Unit,
    onNavigateToGoals: (pigId: Long) -> Unit,
    onNavigateToPigDetail: (pigId: Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentPig = uiState.selectedPig ?: uiState.primaryPig
    val currentPigId = currentPig?.id ?: 1L
    val progression = currentPig?.progression
    val state = currentPig?.state ?: PigState.NEW

    var previousBalance by remember { mutableStateOf<Money?>(null) }
    var pigReactionTrigger by remember { mutableIntStateOf(0) }
    var showCreatePigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.totalBalance) {
        val prev = previousBalance
        if (prev != null && uiState.totalBalance.paise > prev.paise) {
            pigReactionTrigger++
        }
        previousBalance = uiState.totalBalance
    }

    if (showCreatePigDialog) {
        CreatePigDialog(
            onDismiss = { showCreatePigDialog = false },
            onConfirm = { name, targetAmount ->
                viewModel.onCreatePig(name, targetAmount)
                showCreatePigDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "CLINK 🐷",
                actions = {
                    IconButton(
                        onClick = { onNavigateToGoals(currentPigId) },
                        modifier = Modifier.size(ClinkDimens.current.minTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Goals",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToHistory(currentPigId) },
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
                onClick = { onNavigateToAddMoney(currentPigId) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = ClinkDimens.current.spacingLg),
            verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingLg)
        ) {
            item {
                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
            }

            // 0. Pig Selector Row
            item {
                PigSelectorRow(
                    pigs = uiState.allPigs,
                    selectedPigId = currentPigId,
                    onSelectPig = { pigId -> viewModel.onSelectPig(pigId) },
                    onNewPigClick = { showCreatePigDialog = true }
                )
            }

            // 1. Primary Pig Hero Card
            item {
                ClinkCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = ClinkDimens.current.elevationLevel2,
                    onClick = { onNavigateToPigDetail(currentPigId) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ClinkDimens.current.spacingXl)
                            .semantics {
                                contentDescription = "Pig is ${state.displayName}. Total savings ${uiState.totalBalance.formatDisplay()}. Tap to view details."
                            }
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
                                        text = (currentPig?.name ?: "PRIMARY PIG").uppercase(Locale.getDefault()),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                    Text(
                                        text = state.badgeLabel,
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

                                Text(
                                    text = "TOTAL SAVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )

                                AnimatedMoneyDisplay(
                                    money = uiState.totalBalance,
                                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                                Text(
                                    text = if (state == PigState.NEW) {
                                        "Your pig is waiting for its first clink."
                                    } else {
                                        state.description
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

                            // Dynamic state-aware mascot illustration
                            ClinkPigIllustration(
                                size = 88.dp,
                                state = state,
                                reactionTrigger = pigReactionTrigger,
                                contentDescription = "Pig is ${state.displayName}"
                            )
                        }

                        // Progression Tier Indicator
                        if (progression != null) {
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

                            if (progression.nextThreshold != null) {
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progression.progressToNextTier,
                                    animationSpec = tween(durationMillis = 600),
                                    label = "progressAnimation"
                                )

                                LinearProgressIndicator(
                                    progress = { animatedProgress },
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
                                    val targetName = when (progression.state) {
                                        PigState.NEW, PigState.GROWING -> "Healthy Pig"
                                        PigState.HEALTHY -> "Full Pig"
                                        PigState.FULL -> "Max Tier"
                                    }

                                    Text(
                                        text = "${(progression.progressToNextTier * 100).toInt()}% • Next: ${progression.nextThreshold.formatDisplay()} ($targetName)",
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
                            } else {
                                // Full tier: meaningful milestone achievement without fake 100% bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = ClinkDimens.current.spacingMd, vertical = ClinkDimens.current.spacingSm),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "🏆 Top Tier Achieved! Your piggy bank is full and thriving.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Pig Details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. In-Dashboard Primary Save Action Button
            item {
                ClinkButton(
                    text = if (state == PigState.NEW) "Save First ₹10 🐷" else "Save Money 🐷",
                    onClick = { onNavigateToAddMoney(currentPigId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Quick Shortcuts (History & Goals)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd)
                ) {
                    ClinkCard(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        elevation = ClinkDimens.current.elevationLevel1,
                        onClick = { onNavigateToHistory(currentPigId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ClinkDimens.current.spacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(ClinkDimens.current.iconSm)
                                )
                            }
                            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingSm))
                            Column {
                                Text(
                                    text = "History",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "All transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    ClinkCard(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        elevation = ClinkDimens.current.elevationLevel1,
                        onClick = { onNavigateToGoals(currentPigId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ClinkDimens.current.spacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(ClinkDimens.current.iconSm)
                                )
                            }
                            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingSm))
                            Column {
                                Text(
                                    text = "Goals",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Savings targets",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3.5. Compact Active Goal Summary (if user has a goal)
            uiState.activeGoal?.let { goalProgress ->
                item {
                    ClinkCard(
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        elevation = ClinkDimens.current.elevationLevel1,
                        onClick = { onNavigateToGoals(currentPigId) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ClinkDimens.current.spacingMd)
                                .semantics {
                                    contentDescription = "Active goal: ${goalProgress.goal.title}, ${goalProgress.progressPercent} percent complete. Tap to view goals."
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎯 YOUR GOAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    if (goalProgress.isCompleted) {
                                        Spacer(modifier = Modifier.width(ClinkDimens.current.spacingXs))
                                        Text(
                                            text = "Completed 🎉",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clip(MaterialTheme.shapes.extraSmall)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "View Goals",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goalProgress.goal.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${goalProgress.progressPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                            LinearProgressIndicator(
                                progress = { goalProgress.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(MaterialTheme.shapes.small),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MoneyDisplay(
                                        money = goalProgress.currentAmount,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = " / ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    MoneyDisplay(
                                        money = goalProgress.targetAmount,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = if (goalProgress.isCompleted) "Achieved!" else "${goalProgress.remainingAmount.formatDisplay()} left",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = if (goalProgress.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 4. Recent Activity Section
            item {
                ClinkSectionHeader(
                    title = "Recent Activity",
                    subtitle = "Latest savings events",
                    actionText = if (uiState.recentTransactions.isNotEmpty()) "View All" else null,
                    onActionClick = { onNavigateToHistory(currentPigId) }
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    ClinkCard(
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        elevation = ClinkDimens.current.elevationLevel1
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ClinkDimens.current.spacingLg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your pig is waiting for its first clink.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                            Text(
                                text = "Start with ₹10, ₹20, or whatever feels comfortable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentTransactions, key = { it.id }) { tx ->
                    RecentTransactionItem(
                        transaction = tx,
                        onClick = { onNavigateToHistory(currentPigId) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxxl))
            }
        }
    }
}

@Composable
fun PigSelectorRow(
    pigs: List<Pig>,
    selectedPigId: Long,
    onSelectPig: (Long) -> Unit,
    onNewPigClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(pigs, key = { it.id }) { pig ->
            val isSelected = pig.id == selectedPigId
            Surface(
                onClick = { onSelectPig(pig.id) },
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                tonalElevation = if (isSelected) ClinkDimens.current.elevationLevel2 else ClinkDimens.current.elevationLevel0,
                modifier = Modifier
                    .height(48.dp)
                    .semantics {
                        contentDescription = "${pig.name}, ${if (isSelected) "selected" else "not selected"}, balance ${pig.balance.formatDisplay()}"
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = ClinkDimens.current.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingXs)
                ) {
                    Text(
                        text = "🐷",
                        fontSize = 16.sp
                    )
                    Text(
                        text = pig.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pig.balance.formatDisplay(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Surface(
                onClick = onNewPigClick,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .height(48.dp)
                    .semantics {
                        contentDescription = "Create new pig"
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = ClinkDimens.current.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingXs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(ClinkDimens.current.iconSm)
                    )
                    Text(
                        text = "New Pig",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePigDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Money?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Savings Pig 🐷",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd)
            ) {
                Text(
                    text = "Give your new pig a name to keep its savings separate from your other goals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Pig Name (e.g. Emergency Fund)") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            targetText = input
                        }
                    },
                    label = { Text("Target Amount in ₹ (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ClinkButton(
                text = "Create Pig",
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        errorText = "Pig name cannot be empty"
                    } else if (trimmed.length > 30) {
                        errorText = "Pig name cannot exceed 30 characters"
                    } else {
                        val target = targetText.toLongOrNull()?.let { Money.fromRupees(it) }
                        onConfirm(trimmed, target)
                    }
                }
            )
        },
        dismissButton = {
            ClinkOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun RecentTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val formattedDate = formatRelativeTimestamp(transaction.timestamp)

    ClinkCard(
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = ClinkDimens.current.elevationLevel1,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClinkDimens.current.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(ClinkDimens.current.iconSm)
                )
            }

            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note.ifEmpty { "Clink savings" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                MoneyDisplay(
                    money = transaction.amount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun formatRelativeTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val txTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val isSameYear = now.get(Calendar.YEAR) == txTime.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - txTime.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameYear && dayDiff == 0 -> "Today, $timeFormat"
        isSameYear && dayDiff == 1 -> "Yesterday, $timeFormat"
        else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

