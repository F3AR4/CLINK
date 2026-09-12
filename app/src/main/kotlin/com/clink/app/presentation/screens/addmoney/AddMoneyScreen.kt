package com.clink.app.presentation.screens.addmoney

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.clink.app.presentation.components.ClinkAmountChip
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens

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
                .padding(ClinkDimens.current.spacingXl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Piggy mascot banner
            ClinkPigIllustration(size = 88.dp)

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            Text(
                text = "Feed your Piggy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Small micro-savings make big habits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            // Amount Display Card
            ClinkCard(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = ClinkDimens.current.elevationLevel2
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ClinkDimens.current.spacingXl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SAVING AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))
                    MoneyDisplay(
                        money = uiState.selectedAmount,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            Text(
                text = "Quick Select Denomination",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Quick Amount Chips Grid: ₹10, ₹20, ₹50, ₹100
            val amounts = listOf(Money.RS_10, Money.RS_20, Money.RS_50, Money.RS_100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
            ) {
                amounts.forEach { amount ->
                    val isSelected = uiState.selectedAmount == amount
                    ClinkAmountChip(
                        amount = amount,
                        isSelected = isSelected,
                        onClick = { viewModel.onSelectAmount(amount) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Primary Save Action Button
            ClinkButton(
                text = if (uiState.isLoading) "Clinking..." else "Clink It! 🐷",
                onClick = { viewModel.onAddMoney() },
                enabled = !uiState.isProcessing,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))
        }
    }
}
