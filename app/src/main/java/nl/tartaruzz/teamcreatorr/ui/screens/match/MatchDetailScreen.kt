package nl.tartaruzz.teamcreatorr.ui.screens.match

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.MatchStatus
import nl.tartaruzz.teamcreatorr.domain.ShareTextBuilder
import nl.tartaruzz.teamcreatorr.ui.components.ResultBadge
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.TeamCard
import nl.tartaruzz.teamcreatorr.ui.components.statusColor
import nl.tartaruzz.teamcreatorr.ui.components.statusLabel
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.MatchDetailViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(matchId: Long, onBack: () -> Unit) {
    val vm: MatchDetailViewModel = appViewModel()
    LaunchedEffect(matchId) { vm.load(matchId) }
    val match by vm.match.collectAsState()
    val context = LocalContext.current

    var editingResult by remember(matchId) { mutableStateOf(false) }
    var scoreAText by remember(matchId) { mutableStateOf("") }
    var scoreBText by remember(matchId) { mutableStateOf("") }
    var initialized by remember(matchId) { mutableStateOf(false) }
    var showEditDetails by remember(matchId) { mutableStateOf(false) }
    var showDelete by remember(matchId) { mutableStateOf(false) }

    LaunchedEffect(match) {
        val m = match
        if (m != null && !initialized) {
            scoreAText = m.scoreA?.toString() ?: ""
            scoreBText = m.scoreB?.toString() ?: ""
            editingResult = m.status == MatchStatus.PLAYED
            initialized = true
        }
    }
    LaunchedEffect(match?.status) {
        val m = match ?: return@LaunchedEffect
        if (m.status == MatchStatus.PLAYED && !editingResult) {
            scoreAText = ""
            scoreBText = ""
            editingResult = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wedstrijd") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { padding ->
        val m = match
        if (m == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                Formatters.formatLongDate(m.date),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${m.teamSize} tegen ${m.teamSize}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            m.note?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                statusLabel(m.status),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = statusColor(m.status)
                            )
                        }
                    }
                }

                item {
                    SectionTitle("Teams")
                    TeamCard("Team A", TeamAColor, m.teamA)
                }
                item {
                    TeamCard("Team B", TeamBColor, m.teamB)
                }

                item {
                    OutlinedButton(
                        onClick = { shareTeams(context, m) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Deel teamindeling")
                    }
                }

                when {
                    m.status == MatchStatus.CONFIRMED -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "Deze wedstrijd moet nog gespeeld worden. Markeer hem als gespeeld zodra de wedstrijd heeft plaatsgevonden.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Button(onClick = { vm.markPlayed(m.id) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Markeer als gespeeld")
                                    }
                                }
                            }
                        }
                    }
                    m.status == MatchStatus.FINISHED && !editingResult -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "Team A ${m.scoreA} - ${m.scoreB} Team B",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ResultBadge(m)
                                    OutlinedButton(onClick = {
                                        scoreAText = m.scoreA?.toString() ?: ""
                                        scoreBText = m.scoreB?.toString() ?: ""
                                        editingResult = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.size(6.dp))
                                        Text("Uitslag aanpassen")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "Uitslag invoeren",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = scoreAText,
                                            onValueChange = { scoreAText = it },
                                            label = { Text("Score Team A") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("—", style = MaterialTheme.typography.titleLarge)
                                        OutlinedTextField(
                                            value = scoreBText,
                                            onValueChange = { scoreBText = it },
                                            label = { Text("Score Team B") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val a = scoreAText.toIntOrNull()
                                            val b = scoreBText.toIntOrNull()
                                            if (a != null && b != null && a >= 0 && b >= 0) {
                                                vm.saveResult(m.id, a, b)
                                                editingResult = false
                                                Toast.makeText(context, "Uitslag opgeslagen", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Vul geldige scores in", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.size(6.dp))
                                        Text("Uitslag opslaan")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showEditDetails = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Gegevens")
                        }
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Verwijderen")
                        }
                    }
                }
            }
        }
    }

    if (showEditDetails && match != null) {
        var editDate by remember(match!!.id) { mutableStateOf(match!!.date) }
        var editNote by remember(match!!.id) { mutableStateOf(match!!.note ?: "") }
        val cal = Calendar.getInstance().apply { timeInMillis = editDate }
        AlertDialog(
            onDismissRequest = { showEditDetails = false },
            title = { Text("Wedstrijd aanpassen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, mo, d ->
                                    val c = Calendar.getInstance()
                                    c.set(y, mo, d)
                                    editDate = c.timeInMillis
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
                        Text(Formatters.formatDate(editDate))
                    }
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("Notitie (optioneel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.editDetails(match!!.id, editDate, editNote)
                    showEditDetails = false
                    Toast.makeText(context, "Wedstrijd bijgewerkt", Toast.LENGTH_SHORT).show()
                }) { Text("Opslaan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDetails = false }) { Text("Annuleren") }
            }
        )
    }

    if (showDelete && match != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Wedstrijd verwijderen") },
            text = {
                Text(
                    if (match!!.status == MatchStatus.FINISHED) {
                        "Weet je zeker dat je deze wedstrijd wilt verwijderen? Alle punten van deze wedstrijd verdwijnen uit de ranking."
                    } else {
                        "Weet je zeker dat je deze (nog niet gespeelde) wedstrijd wilt annuleren?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(match!!.id)
                    showDelete = false
                    onBack()
                }) { Text("Verwijderen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Annuleren") }
            }
        )
    }
}

private fun shareTeams(context: android.content.Context, match: MatchDetail) {
    val text = ShareTextBuilder.teamIndeling(match.teamA, match.teamB)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Teamindeling delen"))
}

