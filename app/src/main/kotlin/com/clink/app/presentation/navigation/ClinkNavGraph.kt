package com.clink.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clink.app.presentation.screens.addmoney.AddMoneyScreen
import com.clink.app.presentation.screens.goals.CreateGoalScreen
import com.clink.app.presentation.screens.goals.GoalScreen
import com.clink.app.presentation.screens.history.HistoryScreen
import com.clink.app.presentation.screens.home.HomeScreen
import com.clink.app.presentation.screens.onboarding.OnboardingScreen
import com.clink.app.presentation.screens.pigdetail.PigDetailScreen

@Composable
fun ClinkNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddMoney = { pigId ->
                    navController.navigate(Screen.AddMoney.createRoute(pigId))
                },
                onNavigateToHistory = { pigId ->
                    navController.navigate(Screen.History.createRoute(pigId))
                },
                onNavigateToGoals = { pigId ->
                    navController.navigate(Screen.Goals.createRoute(pigId))
                },
                onNavigateToPigDetail = { pigId ->
                    navController.navigate(Screen.PigDetail.createRoute(pigId))
                }
            )
        }

        composable(
            route = Screen.AddMoney.route,
            arguments = listOf(
                navArgument("pigId") {
                    type = NavType.StringType
                    defaultValue = "1"
                }
            )
        ) {
            AddMoneyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(
                navArgument("pigId") {
                    type = NavType.StringType
                    defaultValue = "1"
                }
            )
        ) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMoney = { navController.navigate(Screen.AddMoney.createRoute()) }
            )
        }

        composable(
            route = Screen.Goals.route,
            arguments = listOf(
                navArgument("pigId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            GoalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateGoal = { pigId ->
                    navController.navigate(Screen.CreateGoal.createRoute(pigId))
                }
            )
        }

        composable(
            route = Screen.CreateGoal.route,
            arguments = listOf(
                navArgument("pigId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            CreateGoalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PigDetail.route,
            arguments = listOf(
                navArgument("pigId") {
                    type = NavType.StringType
                }
            )
        ) {
            PigDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMoney = { pigId ->
                    navController.navigate(Screen.AddMoney.createRoute(pigId))
                },
                onNavigateToHistory = { pigId ->
                    navController.navigate(Screen.History.createRoute(pigId))
                },
                onNavigateToGoals = {
                    navController.navigate(Screen.Goals.createRoute())
                }
            )
        }
    }
}
