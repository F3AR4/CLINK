package com.clink.app.presentation.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clink.app.presentation.components.ClinkButton
import com.clink.app.presentation.components.ClinkCard
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.theme.ClinkDimens

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.onDismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ClinkDimens.current.spacingXl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Brand Header & Mascot
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "CLINK Welcome Mascot" }
            ) {
                ClinkPigIllustration(
                    size = 140.dp,
                    modifier = Modifier.padding(bottom = ClinkDimens.current.spacingMd)
                )

                Text(
                    text = "Welcome to CLINK",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXs))

                Text(
                    text = "Save small. Build habits. Clink!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingLg))

            // Value Propositions Card
            ClinkCard(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = ClinkDimens.current.elevationLevel1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ClinkDimens.current.spacingLg),
                    verticalArrangement = Arrangement.spacedBy(ClinkDimens.current.spacingMd)
                ) {
                    OnboardingFeatureRow(
                        badgeColor = MaterialTheme.colorScheme.primary,
                        title = "Save Tiny",
                        description = "Put away ₹10, ₹20, ₹50, or ₹100 whenever you like. Tiny drops fill the pig."
                    )

                    OnboardingFeatureRow(
                        badgeColor = MaterialTheme.colorScheme.secondary,
                        title = "Your Digital Piggy",
                        description = "Your personal offline pig bank, tracking every deposit and celebration."
                    )

                    OnboardingFeatureRow(
                        badgeColor = MaterialTheme.colorScheme.tertiary,
                        title = "Build Lasting Habits",
                        description = "Guilt-free savings designed around your rhythm. No minimums, no stress."
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClinkDimens.current.spacingXl))

            // Call to Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ClinkDimens.current.spacingLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClinkButton(
                    text = "Start Saving",
                    onClick = {
                        viewModel.onCompleteOnboarding(onSuccess = onFinishOnboarding)
                    },
                    isLoading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClinkDimens.current.spacingSm))

                Text(
                    text = "100% private, local & offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    badgeColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(ClinkDimens.current.spacingMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
