package nl.tartaruzz.teamcreatorr.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.ui.navigation.Routes
import nl.tartaruzz.teamcreatorr.ui.screens.home.HomeScreen
import nl.tartaruzz.teamcreatorr.ui.screens.match.MatchDetailScreen
import nl.tartaruzz.teamcreatorr.ui.screens.match.MatchListScreen
import nl.tartaruzz.teamcreatorr.ui.screens.match.NewMatchScreen
import nl.tartaruzz.teamcreatorr.ui.screens.players.PlayerListScreen
import nl.tartaruzz.teamcreatorr.ui.screens.players.PlayerProfileScreen
import nl.tartaruzz.teamcreatorr.ui.screens.ranking.RankingScreen
import nl.tartaruzz.teamcreatorr.ui.screens.settings.SettingsScreen

private data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Routes.PLAYERS, R.string.nav_players, Icons.Filled.Groups),
    BottomNavItem(Routes.WEDSTRIJD, R.string.nav_match, Icons.Filled.SportsSoccer),
    BottomNavItem(Routes.RANKING, R.string.nav_ranking, Icons.Filled.EmojiEvents),
    BottomNavItem(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings)
)

@Composable
fun TeamCreatorrRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 76.dp)
        ) {
            AppNavHost(navController)
        }
        FloatingPillNavBar(
            items = bottomNavItems,
            currentRoute = currentRoute,
            onSelect = { item ->
                if (currentRoute != item.route) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun FloatingPillNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                FloatingPillItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun FloatingPillItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(item.labelRes)
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(220),
        label = "container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "content"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            item.icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPlayers = { navController.navigate(Routes.PLAYERS) },
                onOpenMatch = { id -> navController.navigate(Routes.match(id)) },
                onOpenRanking = {
                    navController.navigate(Routes.RANKING) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Routes.WEDSTRIJD) {
            MatchListScreen(
                onNewMatch = { navController.navigate(Routes.NEW_MATCH) },
                onOpenMatch = { id -> navController.navigate(Routes.match(id)) }
            )
        }
        composable(Routes.NEW_MATCH) {
            NewMatchScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PLAYERS) {
            PlayerListScreen(onOpenPlayer = { id -> navController.navigate(Routes.player(id)) })
        }
        composable(Routes.RANKING) {
            RankingScreen(onOpenPlayer = { id -> navController.navigate(Routes.player(id)) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
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
