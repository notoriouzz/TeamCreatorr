package nl.tartaruzz.teamcreatorr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.ui.components.EmptyState
import nl.tartaruzz.teamcreatorr.ui.components.ResultBadge
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.statusColor
import nl.tartaruzz.teamcreatorr.ui.components.statusLabel
import nl.tartaruzz.teamcreatorr.ui.theme.*
import nl.tartaruzz.teamcreatorr.ui.viewmodel.HomeViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

@Composable
fun HomeScreen(
    onOpenPlayers: () -> Unit,
    onNewMatch: () -> Unit,
    onOpenMatch: (Long) -> Unit,
    onOpenRanking: () -> Unit
) {
    val vm: HomeViewModel = appViewModel()
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header(onNewMatch) }

        if (!state.loading && state.totalPlayers == 0) {
            item {
                EmptyState(
                    title = "Welkom bij TeamCreatorr!",
                    subtitle = "Voeg eerst spelers toe om wedstrijden te maken en een ranking bij te houden.",
                    icon = {
                        Icon(
                            Icons.Filled.SportsSoccer,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = PitchGreen
                        )
                    }
                )
            }
            item {
                Button(onClick = onOpenPlayers, modifier = Modifier.fillMaxWidth()) {
                    Text("Spelers toevoegen")
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Stat("Spelers", state.totalPlayers.toString(), Modifier.weight(1f))
                    Stat("Actief", state.activePlayers.toString(), Modifier.weight(1f))
                    Stat("Wedstrijden", state.totalMatches.toString(), Modifier.weight(1f))
                }
            }

            item {
                SectionTitle("Top 3")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.topThree.isEmpty()) {
                        Text("Nog geen ranking beschikbaar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val medals = listOf(Gold, Silver, Bronze)
                        state.topThree.forEachIndexed { index, player ->
                            TopPlayerCard(index + 1, player, medals[index])
                        }
                    }
                }
            }

            item {
                SectionTitle("Nog te spelen")
                if (state.upcoming.isEmpty()) {
                    Text("Geen geplande wedstrijden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.upcoming.forEach { match ->
                            UpcomingMatchCard(match, onClick = { onOpenMatch(match.id) })
                        }
                    }
                }
            }

            item {
                SectionTitle("Laatste uitslag")
                if (state.latestMatch == null) {
                    Text("Nog geen afgeronde wedstrijden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val match = state.latestMatch!!
                    LatestMatchCard(match, onClick = { onOpenMatch(match.id) })
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenPlayers, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Spelers")
                    }
                    OutlinedButton(onClick = onOpenRanking, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Ranking")
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onNewMatch: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PitchGreen)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SportsSoccer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column {
                    Text(
                        "TeamCreatorr",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Zaalvoetbal Teams & Ranking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNewMatch, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Nieuwe wedstrijd")
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopPlayerCard(position: Int, player: PlayerStats, medalColor: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text("$position", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }
            Column(Modifier.weight(1f)) {
                Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${player.wins} winst · ${player.losses} verlies · ${player.draws} gelijk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${player.totalPoints} pt",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UpcomingMatchCard(match: MatchDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formatters.formatDate(match.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${match.teamSize} tegen ${match.teamSize}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Teams staan klaar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                statusLabel(match.status),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statusColor(match.status)
            )
        }
    }
}

@Composable
private fun LatestMatchCard(match: MatchDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                Formatters.formatDate(match.date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                scoreText(match),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            ResultBadge(match)
        }
    }
}

internal fun scoreText(match: MatchDetail): String =
    if (match.hasResult) "Team A ${match.scoreA} - ${match.scoreB} Team B"
    else "Team A - Team B"


