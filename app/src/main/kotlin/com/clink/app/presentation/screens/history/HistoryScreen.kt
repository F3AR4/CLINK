package com.clink.app.presentation.screens.history

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.domain.model.TransactionType
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddMoney: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = uiState.pigName?.let { "History • $it" } ?: "Saving History",
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    HistoryErrorState(
                        errorMessage = uiState.errorMessage ?: "Couldn't load your savings",
                        onRetry = { viewModel.retry() }
                    )
                }

                uiState.transactions.isEmpty() -> {
                    Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))
                    ClinkEmptyState(
                        title = "No savings yet",
                        description = "Your little savings journey\nwill show up here.",
                        actionButtonText = "Save Your First ₹10",
                        onActionClick = onNavigateToAddMoney
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(key = "summary_card") {
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))
                            HistorySummaryCard(
                                totalSaved = uiState.totalSaved,
                                transactionCount = uiState.transactionCount
                            )
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))
                        }

                        uiState.groupedTransactions.forEach { (header, itemsInGroup) ->
                            item(key = "header_$header") {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(
                                        top = ClinkDimens.current.spacingMd,
                                        bottom = ClinkDimens.current.spacingXs
                                    )
                                )
                            }

                            items(itemsInGroup, key = { it.transaction.id }) { uiModel ->
                                TransactionItem(uiModel = uiModel)
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySummaryCard(
    totalSaved: com.clink.app.domain.model.Money,
    transactionCount: Int
) {
    ClinkCard(
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        elevation = ClinkDimens.current.elevationLevel1
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClinkDimens.current.spacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TOTAL SAVED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                MoneyDisplay(
                    money = totalSaved,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(
                        horizontal = ClinkDimens.current.spacingMd,
                        vertical = ClinkDimens.current.spacingSm
                    )
            ) {
                Text(
                    text = if (transactionCount == 1) "1 saving" else "$transactionCount savings",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TransactionItem(uiModel: TransactionUiModel) {
    val transaction = uiModel.transaction
    val isCredit = transaction.type == TransactionType.CREDIT
    val icon = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconBg = if (isCredit) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
    val iconColor = if (isCredit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

    ClinkCard(
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = ClinkDimens.current.elevationLevel1,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = uiModel.accessibilityDescription
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClinkDimens.current.spacingLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(ClinkDimens.current.iconSm)
                )
            }

            Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note.ifEmpty { if (isCredit) "Savings Added" else "Withdrawal" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCredit) "Saved" else "Withdrew",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(ClinkDimens.current.spacingXs))
                    Text(
                        text = uiModel.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCredit) "+ " else "- ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
                MoneyDisplay(
                    money = transaction.amount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HistoryErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ClinkDimens.current.spacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

        Text(
            text = "Couldn't load your savings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

        ClinkButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
    }
}
