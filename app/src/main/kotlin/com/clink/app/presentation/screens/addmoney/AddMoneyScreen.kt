package com.clink.app.presentation.screens.addmoney

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.domain.model.Money
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay

@Composable
fun AddMoneyScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AddMoneyEvent.Success -> {
                    Toast.makeText(context, "Added to CLINK! 🎉", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is AddMoneyEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "Add to CLINK",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Amount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            MoneyDisplay(
                money = uiState.selectedAmount,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Quick Amount Selector Buttons: ₹10, ₹20, ₹50, ₹100
            val amounts = listOf(Money.RS_10, Money.RS_20, Money.RS_50, Money.RS_100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                amounts.forEach { amount ->
                    val isSelected = uiState.selectedAmount == amount
                    QuickAmountChip(
                        amount = amount,
                        isSelected = isSelected,
                        onClick = { viewModel.onSelectAmount(amount) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ClinkButton(
                text = if (uiState.isLoading) "Processing..." else "Clink It! 🐷",
                onClick = { viewModel.onAddMoney() },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickAmountChip(
    amount: Money,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "₹${amount.paise / 100}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
