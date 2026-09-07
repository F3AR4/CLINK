package com.clink.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object AddMoney : Screen("add_money?pigId={pigId}") {
        fun createRoute(pigId: Long): String = "add_money?pigId=$pigId"
    }
    data object History : Screen("history?pigId={pigId}") {
        fun createRoute(pigId: Long): String = "history?pigId=$pigId"
    }
    data object Goals : Screen("goals")
    data object PigDetail : Screen("pig_detail/{pigId}") {
        fun createRoute(pigId: Long): String = "pig_detail/$pigId"
    }
}
