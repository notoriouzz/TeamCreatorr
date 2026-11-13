package nl.tartaruzz.teamcreatorr.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.MatchStatus
import nl.tartaruzz.teamcreatorr.ui.components.ResultBadge
import nl.tartaruzz.teamcreatorr.ui.components.statusLabel
import nl.tartaruzz.teamcreatorr.ui.components.statusColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.HistoryViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

@Composable
fun HistoryScreen(onOpenMatch: (Long) -> Unit) {
    val vm: HistoryViewModel = appViewModel()
    val matches by vm.matches.collectAsState()

    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Nog geen wedstrijden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Start een nieuwe wedstrijd via het menu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Wedstrijdgeschiedenis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(matches, key = { it.id }) { match ->
                MatchCard(match, onClick = { onOpenMatch(match.id) })
            }
        }
    }
}

@Composable
private fun MatchCard(match: MatchDetail, onClick: () -> Unit) {
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
            if (match.status == MatchStatus.FINISHED) {
                Text(
                    "Team A ${match.scoreA} - ${match.scoreB} Team B",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                ResultBadge(match)
            } else {
                Text(
                    "Nog niet gespeeld",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                statusLabel(match.status),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statusColor(match.status)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                teamLine("Team A", match.teamA),
                style = MaterialTheme.typography.bodySmall,
                color = TeamAColor
            )
            Text(
                teamLine("Team B", match.teamB),
                style = MaterialTheme.typography.bodySmall,
                color = TeamBColor
            )
        }
    }
}

private fun teamLine(name: String, players: List<nl.tartaruzz.teamcreatorr.domain.PlayerRef>): String {
    val names = if (players.isEmpty()) "—" else players.joinToString(", ") { it.name }
    return "$name: $names"
}
