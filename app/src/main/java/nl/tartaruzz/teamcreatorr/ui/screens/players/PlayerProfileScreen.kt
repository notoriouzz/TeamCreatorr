package nl.tartaruzz.teamcreatorr.ui.screens.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.domain.PlayerHistoryItem
import nl.tartaruzz.teamcreatorr.domain.PlayerProfileUiData
import nl.tartaruzz.teamcreatorr.ui.components.EmptyState
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.PlayerProfileViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

private enum class HistoryFilter(val label: String) {
    ALL("Alle"),
    WON("Gewonnen"),
    LOST("Verloren"),
    DRAW("Gelijk")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    playerId: Long,
    onBack: () -> Unit,
    onOpenMatch: (Long) -> Unit
) {
    val vm: PlayerProfileViewModel = appViewModel()
    LaunchedEffect(playerId) { vm.load(playerId) }
    val profile by vm.profile.collectAsState()
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Speler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { padding ->
        val data = profile
        if (data == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ProfileHeader(data) }
                item {
                    SectionTitle("Wedstrijdgeschiedenis")
                    FilterChips(filter) { filter = it }
                }
                val filtered = filteredHistory(data.history, filter)
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "Geen wedstrijden voor dit filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(filtered, key = { it.matchId }) { item ->
                        HistoryItemCard(item, onClick = { onOpenMatch(item.matchId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(data: PlayerProfileUiData) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        data.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(data.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (!data.active) {
                        Text("Inactief", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    val rank = data.stats?.rank
                    if (rank != null) {
                        Text(
                            "Positie: #$rank",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${data.stats?.totalPoints ?: 0}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("punten", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderStat("Deelnames", "${data.stats?.played ?: 0}", Modifier.weight(1f))
                HeaderStat("Winst", "${data.stats?.wins ?: 0}", Modifier.weight(1f))
                HeaderStat("Verlies", "${data.stats?.losses ?: 0}", Modifier.weight(1f))
                HeaderStat("Gelijk", "${data.stats?.draws ?: 0}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Winpercentage: ${data.stats?.winPercentage ?: 0}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilterChips(selected: HistoryFilter, onSelect: (HistoryFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun HistoryItemCard(item: PlayerHistoryItem, onClick: () -> Unit) {
    val own = item.team
    val accent = if (own == "A") TeamAColor else TeamBColor
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().background(accent.copy(alpha = 0.08f)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    Formatters.formatDate(item.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    annotatedScore(item),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            OutcomeLabel(item)
        }
    }
}

@Composable
private fun OutcomeLabel(item: PlayerHistoryItem) {
    val (label, color) = outcome(item)
    Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
}

@Composable
private fun annotatedScore(item: PlayerHistoryItem): androidx.compose.ui.text.AnnotatedString {
    val own = item.team
    return buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = if (own == "A") TeamAColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (own == "A") FontWeight.Bold else FontWeight.Normal
            )
        ) { append("Team A") }
        append(" ${item.scoreA ?: "-"} - ${item.scoreB ?: "-"} ")
        withStyle(
            SpanStyle(
                color = if (own == "B") TeamBColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (own == "B") FontWeight.Bold else FontWeight.Normal
            )
        ) { append("Team B") }
    }
}

@Composable
private fun outcome(item: PlayerHistoryItem): Pair<String, Color> {
    val a = item.scoreA
    val b = item.scoreB
    if (a == null || b == null) return "Geen uitslag" to MaterialTheme.colorScheme.onSurfaceVariant
    if (a == b) return "Gelijkspel" to MaterialTheme.colorScheme.secondary
    val won = (item.team == "A" && a > b) || (item.team == "B" && b > a)
    return if (won) "Winst" to TeamAColor else "Verlies" to TeamBColor
}

private fun filteredHistory(history: List<PlayerHistoryItem>, filter: HistoryFilter): List<PlayerHistoryItem> =
    when (filter) {
        HistoryFilter.ALL -> history
        HistoryFilter.WON -> history.filter { isWin(it) }
        HistoryFilter.LOST -> history.filter { hasResult(it) && !isWin(it) && !isDraw(it) }
        HistoryFilter.DRAW -> history.filter { isDraw(it) }
    }

private fun hasResult(item: PlayerHistoryItem) = item.scoreA != null && item.scoreB != null
private fun isDraw(item: PlayerHistoryItem) = hasResult(item) && item.scoreA == item.scoreB
private fun isWin(item: PlayerHistoryItem) = hasResult(item) &&
    ((item.team == "A" && item.scoreA!! > item.scoreB!!) || (item.team == "B" && item.scoreB!! > item.scoreA!!))
