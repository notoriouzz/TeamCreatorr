package nl.tartaruzz.teamcreatorr.ui.screens.match

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.domain.PlayerRef
import nl.tartaruzz.teamcreatorr.ui.components.DuoManagerSection
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.TeamCard
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.NewMatchViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters
import java.util.Calendar

@Composable
fun NewMatchScreen(onOpenMatch: (Long) -> Unit) {
    val vm: NewMatchViewModel = appViewModel()
    val state by vm.state.collectAsState()
    val players by vm.activePlayers.collectAsState()
    val duos by vm.duos.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.confirmedMatchId) {
        state.confirmedMatchId?.let { id ->
            vm.consumeConfirmation()
            onOpenMatch(id)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Nieuwe wedstrijd", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
            SectionTitle("Datum & notitie")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(state.date) { vm.setDate(it) }
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { vm.setNote(it) },
                    label = { Text("Notitie (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        item {
            DuoManagerSection(
                duos = duos,
                players = players,
                onAdd = { a, b ->
                    vm.addDuo(a, b) { result ->
                        if (result is Repository.DuoResult.Error) {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
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
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (players.isEmpty()) {
                    Text(
                        "Geen actieve spelers. Voeg eerst spelers toe via Home → Spelers.",
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
                enabled = state.canGenerate && !state.confirming
            ) {
                Icon(Icons.Filled.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Teams genereren")
            }
        }

        state.generationError?.let { error ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
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
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.reshuffle() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Opnieuw indelen")
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

