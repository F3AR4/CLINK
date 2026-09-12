package com.clink.app.presentation.screens.goals

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.components.ClinkTopBar
import com.clink.app.presentation.theme.ClinkDimens

@Composable
fun CreateGoalScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateGoalEvent.Success -> {
                    Toast.makeText(context, "Goal created! 🎯", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is CreateGoalEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ClinkTopBar(
                title = "Create Goal",
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
                .verticalScroll(scrollState)
                .padding(horizontal = ClinkDimens.current.spacingXl)
                .padding(bottom = ClinkDimens.current.spacingXl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            ClinkPigIllustration(size = 80.dp)

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

            Text(
                text = "What are you saving for?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Set a clear target to keep your savings motivated.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            // Goal Title Input
            Text(
                text = "Goal Name",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Goal name input" },
                placeholder = { Text("e.g. New Headphones, College Trip") },
                singleLine = true,
                isError = uiState.titleError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.titleError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${uiState.title.length}/50",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))

            // Target Amount Input
            Text(
                text = "Target Amount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

            OutlinedTextField(
                value = uiState.targetAmountText,
                onValueChange = { viewModel.onAmountChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Target amount in Rupees" },
                placeholder = { Text("e.g. 5,000") },
                prefix = {
                    Text(
                        text = "₹ ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                isError = uiState.amountError != null,
                supportingText = {
                    Text(
                        text = uiState.amountError ?: "Enter target in whole Rupees",
                        color = if (uiState.amountError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXxl))

            // Create Action Button
            ClinkButton(
                text = if (uiState.isLoading) "Creating Goal..." else "Create Goal 🎯",
                onClick = { viewModel.onCreateGoal() },
                enabled = uiState.canCreate,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Create goal action" }
            )

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingMd))
        }
    }
}
