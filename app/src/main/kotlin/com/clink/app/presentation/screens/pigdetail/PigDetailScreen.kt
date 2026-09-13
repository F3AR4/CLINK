package com.clink.app.presentation.screens.pigdetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.repository.PigRepository
import com.clink.app.domain.usecase.DeletePigUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
import com.clink.app.domain.usecase.UpdatePigUseCase
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PigDetailViewModel @Inject constructor(
    private val pigRepository: PigRepository,
    observeGoalsUseCase: ObserveGoalsUseCase,
    savedStateHandle: SavedStateHandle,
    private val updatePigUseCase: UpdatePigUseCase? = null,
    private val deletePigUseCase: DeletePigUseCase? = null
) : ViewModel() {
    private val pigId: Long = savedStateHandle.get<String>("pigId")?.toLongOrNull() ?: 1L

    val pig: StateFlow<Pig?> = pigRepository.getPigById(pigId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val canDelete: StateFlow<Boolean> = pigRepository.getAllPigs()
        .map { it.size > 1 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val activeGoal: StateFlow<GoalProgress?> = observeGoalsUseCase(pigId)
        .map { goals -> goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun renamePig(newName: String) {
        viewModelScope.launch {
            updatePigUseCase?.invoke(pigId = pigId, name = newName)
        }
    }

    fun deletePig(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val result = deletePigUseCase?.invoke(pigId)
            if (result?.isSuccess == true) {
                onDeleted()
            }
        }
    }
}

@Composable
fun PigDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddMoney: (pigId: Long) -> Unit,
    onNavigateToHistory: (pigId: Long) -> Unit,
    onNavigateToGoals: () -> Unit = {},
    viewModel: PigDetailViewModel = hiltViewModel()
) {
    val pig by viewModel.pig.collectAsStateWithLifecycle()
    val activeGoal by viewModel.activeGoal.collectAsStateWithLifecycle()
    val canDelete by viewModel.canDelete.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showRenameDialog && pig != null) {
        RenamePigDialog(
            currentName = pig!!.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renamePig(newName)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteDialog && pig != null) {
        DeletePigDialog(
            pigName = pig!!.name,
            canDelete = canDelete,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deletePig {
                    showDeleteDialog = false
                    onNavigateBack()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = pig?.name ?: "Piggy Bank",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (pig != null) {
                        IconButton(
                            onClick = { showRenameDialog = true },
                            modifier = Modifier.size(ClinkDimens.current.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Pig",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(ClinkDimens.current.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Pig",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pig == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                ClinkEmptyState(
                    title = "Pig Not Found",
                    description = "This savings pig may have been deleted or does not exist.",
                    actionButtonText = "Go Back",
                    onActionClick = onNavigateBack
                )
            }
        } else {
            val currentPig = pig!!
            val progression = currentPig.progression

            var previousBalance by remember { mutableStateOf<Money?>(null) }
            var pigReactionTrigger by remember { mutableIntStateOf(0) }

            LaunchedEffect(currentPig.balance) {
                val prev = previousBalance
                if (prev != null && currentPig.balance.paise > prev.paise) {
                    pigReactionTrigger++
                }
                previousBalance = currentPig.balance
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ClinkDimens.current.spacingLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

                // Mascot Hero with current PigState
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    ClinkPigIllustration(
                        size = 88.dp,
                        state = currentPig.state,
                        reactionTrigger = pigReactionTrigger,
                        contentDescription = "Pig is ${currentPig.state.displayName}"
                    )
                }

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

                // Name & State Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
                ) {
                    Text(
                        text = currentPig.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = currentPig.state.badgeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                AnimatedMoneyDisplay(
                    money = currentPig.balance,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

                // Progression Card
                ClinkCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = ClinkDimens.current.elevationLevel2
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ClinkDimens.current.spacingLg)
                    ) {
                        Text(
                            text = "PROGRESSION STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))
                        Text(
                            text = currentPig.state.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

                        LinearProgressIndicator(
                            progress = { progression.progressToNextTier },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )

                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

                        val milestoneText = if (progression.nextThreshold != null) {
                            val nextTierName = when (progression.state) {
                                PigState.NEW, PigState.GROWING -> "Healthy Pig"
                                PigState.HEALTHY -> "Full Pig"
                                PigState.FULL -> "Max Tier"
                            }
                            "Next tier at ${progression.nextThreshold.formatDisplay()} ($nextTierName)"
                        } else {
                            "🏆 Well-fed and thriving! Highest tier reached."
                        }

                        Text(
                            text = milestoneText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Goal Summary Card (if active goal exists)
                activeGoal?.let { goal ->
                    Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

                    ClinkCard(
                        shape = MaterialTheme.shapes.large,
                        containerColor = MaterialTheme.colorScheme.surface,
                        elevation = ClinkDimens.current.elevationLevel1,
                        onClick = onNavigateToGoals
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ClinkDimens.current.spacingLg)
                        ) {
                            ClinkSectionHeader(
                                title = "Savings Target",
                                subtitle = if (goal.isCompleted) "Completed 🎉" else "${goal.progressPercent}% achieved",
                                actionText = "View Goals",
                                onActionClick = onNavigateToGoals
                            )

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

                            Text(
                                text = goal.goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

                            LinearProgressIndicator(
                                progress = { goal.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(MaterialTheme.shapes.small),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${goal.currentAmount.formatDisplay()} / ${goal.targetAmount.formatDisplay()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (goal.isCompleted) "Goal Achieved!" else "${goal.remainingAmount.formatDisplay()} to go",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

                // Pig Info Summary Card
                ClinkCard(
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = ClinkDimens.current.elevationLevel1
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ClinkDimens.current.spacingLg),
                        verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
                    ) {
                        ClinkSectionHeader(
                            title = "Pig Summary",
                            subtitle = null
                        )

                        DetailRow(
                            label = "Status",
                            value = "${currentPig.state.displayName} (${currentPig.state.badgeLabel})"
                        )
                        DetailRow(
                            label = "Current Balance",
                            value = currentPig.balance.formatDisplay()
                        )
                        DetailRow(
                            label = "Target Goal",
                            value = currentPig.targetAmount?.formatDisplay() ?: "No target set"
                        )
                        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(currentPig.createdAt))
                        DetailRow(
                            label = "Created On",
                            value = formattedDate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxl))

                // Action Buttons
                ClinkButton(
                    text = "Add Savings",
                    onClick = { onNavigateToAddMoney(currentPig.id) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

                ClinkOutlinedButton(
                    text = "View Saving History",
                    onClick = { onNavigateToHistory(currentPig.id) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxl))
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RenamePigDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Pig 🐷",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Pig Name") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
                            Text("${name.length}/30", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ClinkButton(
                text = "Save",
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        errorText = "Pig name cannot be empty"
                    } else if (trimmed.length > 30) {
                        errorText = "Pig name cannot exceed 30 characters"
                    } else {
                        onConfirm(trimmed)
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
private fun DeletePigDialog(
    pigName: String,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (canDelete) "Delete Pig?" else "Cannot Delete Pig",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (canDelete) {
                Text(
                    text = "Are you sure you want to delete \"$pigName\"? All associated savings history and goals for this pig will be permanently removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "\"$pigName\" is your only savings pig. CLINK requires at least one active pig. To delete this pig, create another pig first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (canDelete) {
                ClinkButton(
                    text = "Delete",
                    onClick = onConfirm,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            } else {
                ClinkButton(
                    text = "OK",
                    onClick = onDismiss
                )
            }
        },
        dismissButton = {
            if (canDelete) {
                ClinkOutlinedButton(
                    text = "Cancel",
                    onClick = onDismiss
                )
            }
        }
    )
}

