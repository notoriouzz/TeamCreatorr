package nl.tartaruzz.teamcreatorr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.EmptyState
import nl.tartaruzz.teamcreatorr.ui.components.PlayerAvatar
import nl.tartaruzz.teamcreatorr.ui.components.ResultBadge
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.SkeletonBox
import nl.tartaruzz.teamcreatorr.ui.components.statusColor
import nl.tartaruzz.teamcreatorr.ui.components.statusLabel
import nl.tartaruzz.teamcreatorr.ui.theme.*
import nl.tartaruzz.teamcreatorr.ui.viewmodel.HomeViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

@Composable
fun HomeScreen(
    onOpenPlayers: () -> Unit,
    onOpenMatch: (Long) -> Unit,
    onOpenRanking: () -> Unit
) {
    val vm: HomeViewModel = appViewModel()
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AppHeader(stringResource(R.string.app_name), stringResource(R.string.header_subtitle)) }

        if (state.loading) {
            item { SkeletonBox(Modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(20.dp)) }
            item { SkeletonBox(Modifier.fillMaxWidth().height(220.dp)) }
            item { SkeletonBox(Modifier.fillMaxWidth().height(96.dp)) }
        } else if (!state.hasPlayers) {
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
            item { SectionTitle("Eerstvolgende wedstrijd") }
            item {
                val next = state.nextMatch
                if (next == null) {
                    Text(
                        "Geen geplande wedstrijd. Start er één via het tabblad Wedstrijd.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    NextMatchCard(next, onClick = { onOpenMatch(next.id) })
                }
            }

            item { SectionTitle("Ranking") }
            item { RankingPreview(state.topThree, onOpenRanking) }

            item { SectionTitle("Laatste uitslag") }
            item {
                val latest = state.latestMatch
                if (latest == null) {
                    Text("Nog geen afgeronde wedstrijden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LatestMatchCard(latest, onClick = { onOpenMatch(latest.id) })
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PitchGreen)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(54.dp)
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
                    "Teams & Ranking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun NextMatchCard(match: MatchDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SportsSoccer,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    Formatters.formatLongDate(match.date),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${match.teamSize} tegen ${match.teamSize}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
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
private fun RankingPreview(topThree: List<PlayerStats>, onOpenRanking: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            if (topThree.isEmpty()) {
                Text(
                    "Nog geen ranking beschikbaar.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val medals = listOf(Gold, Silver, Bronze)
                topThree.forEachIndexed { index, player ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(30.dp).clip(CircleShape).background(medals[index]),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = "Positie ${index + 1}",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF3A2A00)
                            )
                        }
                        PlayerAvatar(player.name, player.photoFileName, size = 36.dp)
                        Column(Modifier.weight(1f)) {
                            Text(player.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            if (!player.nickname.isNullOrBlank()) {
                                Text(
                                    "\"${player.nickname}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            "${player.totalPoints} pt",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            TextButton(
                onClick = onOpenRanking,
                modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
            ) {
                Text("Volledige ranking")
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LatestMatchCard(match: MatchDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    Formatters.formatDate(match.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    scoreText(match),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            ResultBadge(match)
        }
    }
}

@Composable
internal fun scoreText(match: MatchDetail): String =
    if (match.hasResult) stringResource(R.string.score_format, match.scoreA ?: 0, match.scoreB ?: 0)
    else stringResource(R.string.score_empty)


