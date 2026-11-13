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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
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
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames
import nl.tartaruzz.teamcreatorr.domain.PlayerRef
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
    val duos by vm.globalDuos.collectAsState()
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
            MatchDuoSection(
                useGlobalDuos = state.useGlobalDuos,
                globalDuos = duos,
                excludedGlobalDuoIds = state.excludedGlobalDuoIds,
                tempDuos = state.tempDuos,
                players = players,
                onToggleGlobal = { vm.setUseGlobalDuos(it) },
                onExcludeGlobal = { vm.excludeGlobalDuo(it) },
                onRemoveTemp = { vm.removeTemporaryDuo(it) },
                onAddTemp = { a, b ->
                    if (!vm.addTemporaryDuo(a, b)) {
                        Toast.makeText(context, "Eén van deze spelers zit al in een duo.", Toast.LENGTH_LONG).show()
                    }
                }
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

@Composable
private fun MatchDuoSection(
    useGlobalDuos: Boolean,
    globalDuos: List<DuoWithNames>,
    excludedGlobalDuoIds: Set<Long>,
    tempDuos: List<Pair<Long, Long>>,
    players: List<PlayerEntity>,
    onToggleGlobal: (Boolean) -> Unit,
    onExcludeGlobal: (Long) -> Unit,
    onRemoveTemp: (Int) -> Unit,
    onAddTemp: (Long, Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    val usedIds = remember(useGlobalDuos, excludedGlobalDuoIds, tempDuos) {
        val s = mutableSetOf<Long>()
        if (useGlobalDuos) {
            globalDuos.filter { it.id !in excludedGlobalDuoIds }.forEach {
                s.add(it.playerA.id); s.add(it.playerB.id)
            }
        }
        tempDuos.forEach { s.add(it.first); s.add(it.second) }
        s
    }

    SectionTitle("Duo's")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Gebruik vaste duo's", modifier = Modifier.weight(1f))
        Switch(checked = useGlobalDuos, onCheckedChange = onToggleGlobal)
    }

    var any = false
    if (useGlobalDuos) {
        globalDuos.filter { it.id !in excludedGlobalDuoIds }.forEach { duo ->
            any = true
            DuoRow(
                label = "${duo.playerA.name} + ${duo.playerB.name}",
                tag = "vast duo",
                onRemove = { onExcludeGlobal(duo.id) }
            )
        }
    }
    tempDuos.forEachIndexed { index, pair ->
        any = true
        DuoRow(
            label = "${nameOf(pair.first, players)} + ${nameOf(pair.second, players)}",
            tag = "tijdelijk duo",
            onRemove = { onRemoveTemp(index) }
        )
    }
    if (!any) {
        Text(
            "Geen duo's actief voor deze wedstrijd.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = { showAdd = true }, enabled = players.size >= 2) {
        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text("Duo toevoegen (alleen deze wedstrijd)")
    }

    if (showAdd) {
        AddTempDuoDialog(
            players = players.filter { it.id !in usedIds },
            onDismiss = { showAdd = false },
            onConfirm = { a, b ->
                onAddTemp(a, b)
                showAdd = false
            }
        )
    }
}

@Composable
private fun DuoRow(label: String, tag: String, onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.LinkOff, contentDescription = "Ontkoppel", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AddTempDuoDialog(
    players: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var a by remember { mutableStateOf<PlayerEntity?>(null) }
    var b by remember { mutableStateOf<PlayerEntity?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duo toevoegen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TempDropdown("Speler 1", players, a) { a = it }
                TempDropdown("Speler 2", players, b) { b = it }
                if (players.size < 2) {
                    Text("Niet genoeg vrije spelers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = a != null && b != null && a!!.id != b!!.id,
                onClick = { onConfirm(a!!.id, b!!.id) }
            ) { Text("Koppelen") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleren") }
        }
    )
}

@Composable
private fun TempDropdown(
    label: String,
    players: List<PlayerEntity>,
    selected: PlayerEntity?,
    onSelect: (PlayerEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: label, modifier = Modifier.weight(1f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            players.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = { onSelect(p); expanded = false }
                )
            }
        }
    }
}

private fun nameOf(id: Long, players: List<PlayerEntity>): String =
    players.firstOrNull { it.id == id }?.name ?: "Speler"


