package nl.tartaruzz.teamcreatorr.ui.screens.match

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.domain.PlayerRef
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.DuoMatchManager
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.TeamCard
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.NewMatchViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters
import nl.tartaruzz.teamcreatorr.util.Toasts
import java.util.Calendar

@Composable
fun NewMatchScreen(onBack: () -> Unit) {
    val vm: NewMatchViewModel = appViewModel()
    val state by vm.state.collectAsState()
    val players by vm.activePlayers.collectAsState()
    val duos by vm.duos.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(state.teamA, state.teamB) {
        if (state.teamA.isNotEmpty() && state.teamB.isNotEmpty()) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it > 0 }
            listState.animateScrollToItem((listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(state.confirmedMatchId) {
        if (state.confirmedMatchId != null) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it > 0 }
            listState.animateScrollToItem((listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppHeader(stringResource(R.string.new_match_title), onBack = onBack)
        }

        item {
            SectionTitle("Teamgrootte")
            FormatSelector(state.teamSize) { vm.setTeamSize(it) }
            Text(
                "Er zijn ${state.required} spelers nodig (${state.teamSize} tegen ${state.teamSize}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SectionTitle("Datum")
            DateField(state.date) { vm.setDate(it) }
        }

        item {
            SectionTitle("Evenwichtige teams")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Houd rekening met ranking", modifier = Modifier.weight(1f))
                Switch(checked = state.useRanking, onCheckedChange = { vm.setUseRanking(it) })
            }
            Text(
                "Verdeelt de teams zo evenwichtig mogelijk op basis van spelersratings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            DuoMatchManager(
                duos = duos,
                players = players,
                disabledDuoIds = state.disabledDuoIds,
                onToggleDuo = { vm.toggleDuo(it) },
                onAdd = { a, b, mode ->
                    vm.addDuo(a, b, mode) { result ->
                        if (result is nl.tartaruzz.teamcreatorr.data.Repository.DuoResult.Error) {
                            Toasts.show(context, result.message)
                        }
                    }
                },
                onModeChange = { id, mode -> vm.setDuoMode(id, mode) },
                onRemove = { vm.removeDuo(it) }
            )
        }

        item {
            SectionTitle("Spelers selecteren")
            val selected = state.selected.size
            val required = state.required
            val remaining = required - selected
            val counterColor = if (selected == required) MaterialTheme.colorScheme.primary
            else if (selected > required) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                "$selected / $required geselecteerd",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = counterColor
            )
            if (remaining > 0) {
                Text(
                    "Nog $remaining speler(s) nodig.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (remaining < 0) {
                Text(
                    "${-remaining} speler(s) te veel geselecteerd.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.selectAll() }, modifier = Modifier.weight(1f)) {
                    Text("Iedereen selecteren")
                }
                OutlinedButton(onClick = { vm.clearSelection() }, modifier = Modifier.weight(1f)) {
                    Text("Alles wissen")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (players.isEmpty()) {
                    Text(
                        "Geen actieve spelers. Voeg eerst spelers toe via het tabblad Spelers.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                players.forEach { player ->
                    PlayerCheckRow(
                        player = player,
                        checked = player.id in state.selected,
                        onToggle = { vm.togglePlayer(player.id) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = { vm.generateTeams() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canGenerate && !state.confirming && state.confirmedMatchId == null
            ) {
                Icon(Icons.Filled.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Teams genereren")
            }
        }

        state.generationError?.let { error ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (state.teamA.isNotEmpty() && state.teamB.isNotEmpty()) {
            item {
                SectionTitle("Teamindeling")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TeamCard(
                        teamName = "Team A",
                        accent = TeamAColor,
                        players = toRefs(state.teamA, players),
                        modifier = Modifier.weight(1f)
                    )
                    TeamCard(
                        teamName = "Team B",
                        accent = TeamBColor,
                        players = toRefs(state.teamB, players),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (state.useRanking) {
                item {
                    Text(
                        "Teamscores: Team A ${state.teamARating} · Team B ${state.teamBRating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (state.confirmedMatchId == null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.reshuffle() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.new_match_again))
                        }
                        Button(
                            onClick = { vm.confirm() },
                            modifier = Modifier.weight(1f),
                            enabled = !state.confirming
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(if (state.confirming) "Opslaan..." else "Bevestigen")
                        }
                    }
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Teamindeling opgeslagen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "De wedstrijd blijft nog te spelen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(R.string.new_match_check), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun toRefs(ids: List<Long>, players: List<PlayerEntity>): List<PlayerRef> =
    ids.mapNotNull { id -> players.find { it.id == id }?.let { PlayerRef(it.id, it.name) } }

@Composable
private fun FormatSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (3..11).forEach { size ->
            if (size == selected) {
                Button(onClick = { onSelect(size) }) { Text("$size tegen $size") }
            } else {
                OutlinedButton(onClick = { onSelect(size) }) { Text("$size tegen $size") }
            }
        }
    }
}

@Composable
private fun DateField(date: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    val cal = Calendar.getInstance().apply { timeInMillis = date }
    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val c = Calendar.getInstance()
                    c.set(y, m, d)
                    onChange(c.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text(Formatters.formatDate(date))
    }
}

@Composable
private fun PlayerCheckRow(player: PlayerEntity, checked: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Text(
                player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}



