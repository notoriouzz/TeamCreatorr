package nl.tartaruzz.teamcreatorr.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.PlayerAvatar
import nl.tartaruzz.teamcreatorr.ui.theme.Bronze
import nl.tartaruzz.teamcreatorr.ui.theme.Gold
import nl.tartaruzz.teamcreatorr.ui.theme.Silver
import nl.tartaruzz.teamcreatorr.ui.viewmodel.RankingViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel

@Composable
fun RankingScreen(onOpenPlayer: (Long) -> Unit) {
    val vm: RankingViewModel = appViewModel()
    val ranking by vm.ranking.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AppHeader(stringResource(R.string.ranking_title))
        Spacer(Modifier.height(16.dp))
        if (ranking.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.ranking_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    stringResource(R.string.ranking_empty_sub),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Podium(ranking.take(3), onOpenPlayer) }
                val rest = ranking.drop(3)
                if (rest.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.ranking_rest),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(rest, key = { it.playerId }) { stats ->
                        RankingListRow(stats, onClick = { onOpenPlayer(stats.playerId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Podium(topThree: List<PlayerStats>, onOpenPlayer: (Long) -> Unit) {
    if (topThree.isEmpty()) return
    val display = listOfNotNull(
        topThree.getOrNull(1),
        topThree.getOrNull(0),
        topThree.getOrNull(2)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        display.forEach { stats ->
            val medal = when (stats.rank) {
                1 -> Gold
                2 -> Silver
                else -> Bronze
            }
            val height = when (stats.rank) {
                1 -> 120.dp
                2 -> 96.dp
                else -> 78.dp
            }
            PodiumColumn(stats, medal, height, Modifier.weight(1f), onOpenPlayer)
        }
    }
}

@Composable
private fun PodiumColumn(
    stats: PlayerStats,
    medal: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onOpenPlayer: (Long) -> Unit
) {
    Column(
        modifier = modifier.clickable { onOpenPlayer(stats.playerId) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(medal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = stringResource(R.string.ranking_position, stats.rank),
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF3A2A00)
            )
        }
        Spacer(Modifier.height(6.dp))
        PlayerAvatar(stats.name, stats.photoFileName, size = 56.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            stats.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${stats.totalPoints} ${stringResource(R.string.home_points_suffix)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(medal.copy(alpha = 0.9f)),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                "${stats.rank}",
                Modifier.padding(top = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A2A00)
            )
        }
    }
}

@Composable
private fun RankingListRow(stats: PlayerStats, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${stats.rank}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
            PlayerAvatar(stats.name, stats.photoFileName, size = 40.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stats.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (stats.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!stats.active) {
                        Text(
                            stringResource(R.string.players_inactive),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (!stats.nickname.isNullOrBlank()) {
                    Text(
                        "\"${stats.nickname}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "${stats.totalPoints}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
