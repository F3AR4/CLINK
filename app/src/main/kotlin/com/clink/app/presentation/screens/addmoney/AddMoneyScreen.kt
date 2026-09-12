package com.clink.app.presentation.screens.addmoney

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.PigState
import com.clink.app.presentation.components.AnimatedMoneyDisplay
import com.clink.app.presentation.components.ClinkAmountChip
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkSavingsAnimationOverlay
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.components.MoneyDisplay
import com.clink.app.presentation.theme.ClinkDimens
import com.clink.app.presentation.theme.SuccessGreen

@Composable
fun AddMoneyScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isAnimationActive by remember { mutableStateOf(false) }
    var animatingAmount by remember { mutableStateOf<Money?>(null) }
    var pigReactionTrigger by remember { mutableIntStateOf(0) }

    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var pigCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var buttonCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AddMoneyEvent.Success -> {
                    // Trigger the signature CLINK coin flight and pig reaction animation
                    animatingAmount = event.amount
                    isAnimationActive = true
                    Toast.makeText(
                        context,
                        "CLINK! ${event.amount.formatDisplay()} saved 🐷",
                        Toast.LENGTH_SHORT
                    ).show()
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
                title = uiState.targetPig?.let { "Add to ${it.name}" } ?: "Add to CLINK",
                canNavigateBack = true,
                onNavigateBack = {
                    if (!isAnimationActive) onNavigateBack()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .onGloballyPositioned { rootCoordinates = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = ClinkDimens.current.spacingXl)
                    .padding(bottom = ClinkDimens.current.spacingXl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

                // Piggy mascot banner with presentation reaction trigger and target pig state
                ClinkPigIllustration(
                    size = 80.dp,
                    state = uiState.targetPig?.state ?: PigState.NEW,
                    reactionTrigger = pigReactionTrigger,
                    modifier = Modifier.onGloballyPositioned { pigCoordinates = it }
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

                Text(
                    text = uiState.targetPig?.let { "Saving to ${it.name}" } ?: "Feed your Piggy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Small micro-savings make big habits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Amount Display Card
            ClinkCard(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = ClinkDimens.current.elevationLevel2,
                modifier = Modifier.semantics {
                    contentDescription = "Selected saving amount: ${uiState.selectedAmount.formatDisplay()}"
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ClinkDimens.current.spacingLg, horizontal = ClinkDimens.current.spacingMd),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SAVING AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                    AnimatedMoneyDisplay(
                        money = uiState.selectedAmount,
                        fontSize = 44.sp,
                        color = if (uiState.validationError != null && uiState.selectedAmount.isZero) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )

                    if (uiState.validationError != null) {
                        Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))
                        Text(
                            text = uiState.validationError!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Quick Select Denominations Header
            Text(
                text = "Quick Select Denomination",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

            // Quick Amount Chips Grid: ₹10, ₹20, ₹50, ₹100
            val quickAmounts = listOf(Money.RS_10, Money.RS_20, Money.RS_50, Money.RS_100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingSm)
            ) {
                quickAmounts.forEach { amount ->
                    val isSelected = !uiState.isCustomAmount && uiState.selectedAmount == amount
                    ClinkAmountChip(
                        amount = amount,
                        isSelected = isSelected,
                        onClick = { viewModel.onSelectQuickAmount(amount) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Custom Amount Input
            Text(
                text = "Or Enter Custom Amount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

            OutlinedTextField(
                value = uiState.customAmountText,
                onValueChange = { viewModel.onCustomAmountChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Custom saving amount in Rupees" },
                placeholder = { Text("e.g. 25, 35, 99") },
                prefix = {
                    Text(
                        text = "₹ ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.customAmountText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onCustomAmountChange("") },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear custom amount",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = uiState.isCustomAmount && uiState.validationError != null,
                supportingText = {
                    if (uiState.isCustomAmount && uiState.validationError != null) {
                        Text(
                            text = uiState.validationError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Whole Rupee amount (₹1 – ₹1,00,000)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Note (Optional)
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

            OutlinedTextField(
                value = uiState.noteText,
                onValueChange = { viewModel.onNoteChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Savings note" },
                placeholder = { Text("e.g., Coffee skipped, Pocket money") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                supportingText = {
                    Text(
                        text = "${uiState.noteText.length}/50",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Lightweight Success Celebration Banner
            AnimatedVisibility(
                visible = uiState.saveStatus is SaveStatus.Success,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                ClinkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ClinkDimens.current.spacingSm),
                    containerColor = SuccessGreen.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ClinkDimens.current.spacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(ClinkDimens.current.spacingSm))
                        Text(
                            text = "CLINK! ${uiState.savedAmount?.formatDisplay() ?: ""} saved 🐷",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Primary Save Action Button
            ClinkButton(
                text = when {
                    uiState.isLoading -> "Clinking..."
                    isAnimationActive || uiState.saveStatus is SaveStatus.Success -> "Saved! 🎉"
                    else -> "Clink It! 🐷"
                },
                onClick = { viewModel.onAddMoney() },
                enabled = uiState.canSave && !isAnimationActive,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { buttonCoordinates = it }
                    .semantics {
                        contentDescription = "Save ${uiState.selectedAmount.formatDisplay()} to Pig"
                    }
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))
        }

        // Signature CLINK savings animation overlay
        if (isAnimationActive && animatingAmount != null) {
            val root = rootCoordinates
            val start = if (root != null && buttonCoordinates != null && buttonCoordinates!!.isAttached) {
                root.localPositionOf(
                    buttonCoordinates!!,
                    Offset(buttonCoordinates!!.size.width / 2f, buttonCoordinates!!.size.height / 2f)
                )
            } else {
                Offset(500f, 1500f)
            }

            val target = if (root != null && pigCoordinates != null && pigCoordinates!!.isAttached) {
                root.localPositionOf(
                    pigCoordinates!!,
                    Offset(pigCoordinates!!.size.width / 2f, pigCoordinates!!.size.height * 0.35f)
                )
            } else {
                Offset(500f, 250f)
            }

            ClinkSavingsAnimationOverlay(
                isPlaying = isAnimationActive,
                amount = animatingAmount!!,
                startOffset = start,
                targetOffset = target,
                onPigReaction = {
                    pigReactionTrigger++
                },
                onAnimationComplete = {
                    isAnimationActive = false
                    onNavigateBack()
                }
            )
        }
    }
}
}
