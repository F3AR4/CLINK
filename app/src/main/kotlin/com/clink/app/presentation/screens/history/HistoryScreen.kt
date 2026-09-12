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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Icon
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
import com.clink.app.domain.model.Transaction
import com.clink.app.domain.model.TransactionType
import com.clink.app.domain.usecase.GetTransactionsUseCase
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkEmptyState
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
class HistoryViewModel @Inject constructor(
    getTransactionsUseCase: GetTransactionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val pigId: Long? = savedStateHandle.get<String>("pigId")?.toLongOrNull()

    val transactions: StateFlow<List<Transaction>> = getTransactionsUseCase(pigId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "Saving History",
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

            if (transactions.isEmpty()) {
                ClinkEmptyState(
                    title = "No savings recorded yet",
                    description = "Every journey begins with a single clink! Tap Add Savings to start.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItem(transaction = tx)
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
fun TransactionItem(transaction: Transaction) {
    val isCredit = transaction.type == TransactionType.CREDIT
    val icon = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconBg = if (isCredit) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
    val iconColor = if (isCredit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))

    ClinkCard(
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = ClinkDimens.current.elevationLevel1
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
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
