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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Goal
import com.clink.app.domain.repository.GoalRepository
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    goalRepository: GoalRepository
) : ViewModel() {
    val goals: StateFlow<List<Goal>> = goalRepository.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

@Composable
fun GoalScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "Savings Goals",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
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
                ClinkEmptyState(
                    title = "No goals set yet",
                    description = "Set a target for that new gadget, vacation, or emergency fund!",
                    icon = Icons.Default.TrackChanges
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalItem(goal = goal)
                    }
                    item {
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxl))
                    }
                }
            }
        }
    }
}

@Composable
fun GoalItem(goal: Goal) {
    ClinkCard(
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = ClinkDimens.current.elevationLevel1
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                MoneyDisplay(
                    money = goal.targetAmount,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Rounded Progress Bar
            LinearProgressIndicator(
                progress = { goal.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(goal.progressPercentage * 100).toInt()}% completed",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${goal.savedAmount.formatDisplay()} saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
