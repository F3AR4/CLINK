package com.clink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.clink.app.presentation.components.ClinkPigIllustration
import com.clink.app.presentation.main.MainUiState
import com.clink.app.presentation.main.MainViewModel
import com.clink.app.presentation.navigation.ClinkNavGraph
import com.clink.app.presentation.theme.ClinkTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for CLINK.
 * Observes persistent onboarding state on startup to route to Onboarding or Home
 * without screen flash.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClinkTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                when (val state = uiState) {
                    is MainUiState.Loading -> {
                        StartupSplashScreen()
                    }
                    is MainUiState.Ready -> {
                        val navController = rememberNavController()
                        ClinkNavGraph(
                            navController = navController,
                            startDestination = state.startDestination
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartupSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        ClinkPigIllustration(size = 96.dp)
    }
}
