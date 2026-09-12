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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.clink.app.domain.model.Pig
import com.clink.app.domain.model.PigState
import com.clink.app.domain.repository.PigRepository
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkOutlinedButton
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkSectionHeader
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PigDetailViewModel @Inject constructor(
    pigRepository: PigRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val pigId: Long = savedStateHandle.get<String>("pigId")?.toLongOrNull() ?: 1L

    val pig: StateFlow<Pig?> = pigRepository.getPigById(pigId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

@Composable
fun PigDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddMoney: (pigId: Long) -> Unit,
    onNavigateToHistory: (pigId: Long) -> Unit,
    viewModel: PigDetailViewModel = hiltViewModel()
) {
    val pig by viewModel.pig.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = pig?.name ?: "Piggy Bank",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val currentPig = pig!!
            val progression = currentPig.progression

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

                MoneyDisplay(
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
