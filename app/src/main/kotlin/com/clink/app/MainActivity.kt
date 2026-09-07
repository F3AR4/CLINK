package com.clink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.clink.app.presentation.navigation.ClinkNavGraph
import com.clink.app.presentation.theme.ClinkTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for CLINK.
 * Single-Activity architecture hosting the Compose Navigation Graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClinkTheme {
                val navController = rememberNavController()
                ClinkNavGraph(navController = navController)
            }
        }
    }
}
