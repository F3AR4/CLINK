package com.clink.app.presentation.screens.goals

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.GoalProgress
import com.clink.app.domain.usecase.DeleteGoalUseCase
import com.clink.app.domain.usecase.ObserveGoalsUseCase
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val isLoading: Boolean = false,
    val goals: List<GoalProgress> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val observeGoalsUseCase: ObserveGoalsUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    private val getSelectedPigUseCase: com.clink.app.domain.usecase.GetSelectedPigUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState(isLoading = false))
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    val currentPigId: Long?
        get() = savedStateHandle?.get<String>("pigId")?.toLongOrNull()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<GoalProgress>> = run {
        val explicitPigId = savedStateHandle?.get<String>("pigId")?.toLongOrNull()
        if (explicitPigId != null) {
            observeGoalsUseCase(explicitPigId)
        } else if (getSelectedPigUseCase != null) {
            getSelectedPigUseCase().flatMapLatest { pig ->
                if (pig != null) {
                    observeGoalsUseCase(pig.id)
                } else {
                    observeGoalsUseCase()
                }
            }
        } else {
            observeGoalsUseCase()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = deleteGoalUseCase(goalId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to delete goal"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

@Composable
fun GoalScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateGoal: (Long?) -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var goalToDelete by remember { mutableStateOf<GoalProgress?>(null) }

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "Savings Goals",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (goals.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onNavigateToCreateGoal(viewModel.currentPigId) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.semantics {
                        contentDescription = "Create new savings goal"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
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

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ClinkEmptyState(
                            title = "No goals yet",
                            description = "Give your savings a destination.",
                            icon = Icons.Default.TrackChanges
                        )

                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

                        ClinkButton(
                            text = "Create Your First Goal 🎯",
                            onClick = { onNavigateToCreateGoal(viewModel.currentPigId) },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(goals, key = { it.goal.id }) { goalProgress ->
                        GoalProgressItem(
                            goalProgress = goalProgress,
                            onDeleteClick = { goalToDelete = goalProgress }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    goalToDelete?.let { targetGoal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = {
                Text(
                    text = "Delete Goal?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${targetGoal.goal.title}\"?\n\nYour saved money and transaction history will remain completely safe in your pig.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGoal(targetGoal.goal.id)
                        goalToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { goalToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GoalProgressItem(
    goalProgress: GoalProgress,
    onDeleteClick: () -> Unit
) {
    val isCompleted = goalProgress.isCompleted
    val containerColor = if (isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ClinkCard(
        shape = MaterialTheme.shapes.large,
        containerColor = containerColor,
        elevation = if (isCompleted) 0.dp else ClinkDimens.current.elevationLevel1,
        modifier = Modifier.semantics {
            contentDescription = "${goalProgress.goal.title}, ${goalProgress.progressPercent} percent complete. Saved ${goalProgress.currentAmount.formatDisplay()} of ${goalProgress.targetAmount.formatDisplay()}."
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClinkDimens.current.spacingLg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(ClinkDimens.current.iconSm)
                        )
                    }
                    Spacer(modifier = Modifier.width(ClinkDimens.current.spacingSm))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = goalProgress.goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(ClinkDimens.current.spacingXs))
                                Text(
                                    text = "Completed 🎉",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete ${goalProgress.goal.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    MoneyDisplay(
                        money = goalProgress.currentAmount,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    MoneyDisplay(
                        money = goalProgress.targetAmount,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${goalProgress.progressPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

            LinearProgressIndicator(
                progress = { goalProgress.progressFraction },
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
                    text = if (isCompleted) {
                        "Goal achieved!"
                    } else {
                        "${goalProgress.remainingAmount.formatDisplay()} to go"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
