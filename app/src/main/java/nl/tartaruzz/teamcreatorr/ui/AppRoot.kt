package nl.tartaruzz.teamcreatorr.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.tartaruzz.teamcreatorr.ui.navigation.Routes
import nl.tartaruzz.teamcreatorr.ui.screens.history.HistoryScreen
import nl.tartaruzz.teamcreatorr.ui.screens.home.HomeScreen
import nl.tartaruzz.teamcreatorr.ui.screens.match.MatchDetailScreen
import nl.tartaruzz.teamcreatorr.ui.screens.match.NewMatchScreen
import nl.tartaruzz.teamcreatorr.ui.screens.players.PlayerListScreen
import nl.tartaruzz.teamcreatorr.ui.screens.players.PlayerProfileScreen
import nl.tartaruzz.teamcreatorr.ui.screens.ranking.RankingScreen
import nl.tartaruzz.teamcreatorr.ui.screens.settings.SettingsScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.NEW_MATCH, "Wedstrijd", Icons.Filled.SportsSoccer),
    BottomNavItem(Routes.RANKING, "Ranking", Icons.Filled.EmojiEvents),
    BottomNavItem(Routes.HISTORY, "Historie", Icons.Filled.History),
    BottomNavItem(Routes.SETTINGS, "Instellingen", Icons.Filled.Settings)
)

@Composable
fun TeamCreatorrRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            AppNavHost(navController)
        }
    }
}

@Composable
private fun AppNavHost(navController: androidx.navigation.NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPlayers = { navController.navigate(Routes.PLAYERS) },
                onNewMatch = { navController.navigate(Routes.NEW_MATCH) },
                onOpenMatch = { id -> navController.navigate(Routes.match(id)) },
                onOpenRanking = { navController.navigate(Routes.RANKING) }
            )
        }
        composable(Routes.NEW_MATCH) {
            NewMatchScreen(onOpenMatch = { id -> navController.navigate(Routes.match(id)) })
        }
        composable(Routes.RANKING) {
            RankingScreen(onOpenPlayer = { id -> navController.navigate(Routes.player(id)) })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onOpenMatch = { id -> navController.navigate(Routes.match(id)) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onOpenPlayers = { navController.navigate(Routes.PLAYERS) })
        }
        composable(Routes.PLAYERS) {
            PlayerListScreen(
                onOpenPlayer = { id -> navController.navigate(Routes.player(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.PLAYER,
            arguments = listOf(navArgument("playerId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("playerId") ?: return@composable
            PlayerProfileScreen(
                playerId = id,
                onBack = { navController.popBackStack() },
                onOpenMatch = { matchId -> navController.navigate(Routes.match(matchId)) }
            )
        }
        composable(
            Routes.MATCH,
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("matchId") ?: return@composable
            MatchDetailScreen(
                matchId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
