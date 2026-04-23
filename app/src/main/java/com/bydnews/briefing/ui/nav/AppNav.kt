package com.bydnews.briefing.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bydnews.briefing.ui.home.HomeScreen
import com.bydnews.briefing.ui.player.PlayerScreen
import com.bydnews.briefing.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{id}"
    fun player(id: String) = "player/$id"
}

@Composable
fun AppNav(initialBriefingId: String?) {
    val nav = rememberNavController()
    val start = if (initialBriefingId != null) Routes.player(initialBriefingId) else Routes.HOME
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onPlay = { id -> nav.navigate(Routes.player(id)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.PLAYER) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            PlayerScreen(briefingId = id, onBack = { nav.popBackStack() })
        }
    }
}
