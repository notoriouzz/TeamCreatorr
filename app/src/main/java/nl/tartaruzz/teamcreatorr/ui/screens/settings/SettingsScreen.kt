package nl.tartaruzz.teamcreatorr.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.MatchStatus
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.domain.PointSettings
import nl.tartaruzz.teamcreatorr.ui.components.DuoManagerSection
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.viewmodel.SettingsViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

@Composable
fun SettingsScreen(onOpenPlayers: () -> Unit) {
    val vm: SettingsViewModel = appViewModel()
    val settings by vm.settings.collectAsState()
    val duos by vm.duos.collectAsState()
    val players by vm.players.collectAsState()
    val ranking by vm.ranking.collectAsState()
    val matches by vm.matches.collectAsState()
    val context = LocalContext.current

    var attText by remember { mutableStateOf("") }
    var winText by remember { mutableStateOf("") }
    var drawText by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (!initialized) {
            attText = settings.attendancePoints.toString()
            winText = settings.winPoints.toString()
            drawText = settings.drawPoints.toString()
            initialized = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Instellingen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            SectionTitle("Puntentelling")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = attText,
                            onValueChange = { attText = it },
                            label = { Text("Aanwezigheid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = winText,
                            onValueChange = { winText = it },
                            label = { Text("Winst") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = drawText,
                        onValueChange = { drawText = it },
                        label = { Text("Gelijkspel") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Bij een wijziging wordt de volledige ranking automatisch herberekend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            val a = attText.toIntOrNull()
                            val w = winText.toIntOrNull()
                            val d = drawText.toIntOrNull()
                            if (a != null && w != null && d != null && a >= 0 && w >= 0 && d >= 0) {
                                showConfirm = true
                            } else {
                                Toast.makeText(context, "Vul geldige punten in", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Puntentelling opslaan")
                    }
                }
            }
        }

        item {
            SectionTitle("Duo's")
            DuoManagerSection(
                duos = duos,
                players = players,
                onAdd = { a, b ->
                    vm.addDuo(a, b) { result ->
                        if (result is nl.tartaruzz.teamcreatorr.data.Repository.DuoResult.Error) {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onRemove = { vm.removeDuo(it) }
            )
        }

        item {
            SectionTitle("Gegevens")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val csv = buildCsv(ranking, matches, settings)
                        shareText(context, csv)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Exporteren (CSV)")
                }
                OutlinedButton(onClick = onOpenPlayers, modifier = Modifier.fillMaxWidth()) {
                    Text("Spelers beheren")
                }
            }
        }

        item {
            SectionTitle("Over")
            Text(
                "TeamCreatorr v1.1.0\nZaalvoetbal Teams & Ranking.\nAlle gegevens worden lokaal op dit apparaat opgeslagen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Puntentelling toepassen?") },
            text = {
                Text(
                    "De nieuwe puntentelling (aanwezigheid: ${attText.toIntOrNull() ?: 0}, " +
                        "winst: ${winText.toIntOrNull() ?: 0}, gelijkspel: ${drawText.toIntOrNull() ?: 0}) " +
                        "wordt toegepast op alle spelers en alle wedstrijden."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateSettings(
                        attText.toIntOrNull() ?: 0,
                        winText.toIntOrNull() ?: 0,
                        drawText.toIntOrNull() ?: 0
                    )
                    showConfirm = false
                    Toast.makeText(context, "Puntentelling bijgewerkt", Toast.LENGTH_SHORT).show()
                }) { Text("Toepassen") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Annuleren") }
            }
        )
    }
}

private fun buildCsv(
    ranking: List<PlayerStats>,
    matches: List<MatchDetail>,
    settings: PointSettings
): String {
    val sb = StringBuilder()
    sb.appendLine("TeamCreatorr export")
    sb.appendLine()
    sb.appendLine("PUNTENTELLING")
    sb.appendLine("Aanwezigheid;Winst;Gelijkspel")
    sb.appendLine("${settings.attendancePoints};${settings.winPoints};${settings.drawPoints}")
    sb.appendLine()
    sb.appendLine("RANKING")
    sb.appendLine("Positie;Naam;Deelnames;Winst;Verlies;Gelijk;Aanwezigheid;Winst;Gelijkspel;Handmatig;Totaal")
    ranking.forEach {
        sb.appendLine(
            "${it.rank};${it.name};${it.played};${it.wins};${it.losses};${it.draws};" +
                "${it.attendancePoints};${it.winPoints};${it.drawPoints};${it.manualPoints};${it.totalPoints}"
        )
    }
    sb.appendLine()
    sb.appendLine("WEDSTRIJDEN")
    sb.appendLine("Datum;Status;Team A;Team B;Score A;Score B")
    matches.forEach {
        sb.appendLine(
            "${Formatters.formatDate(it.date)};${statusName(it.status)};" +
                "${it.teamA.joinToString(", ") { p -> p.name }};" +
                "${it.teamB.joinToString(", ") { p -> p.name }};${it.scoreA ?: ""};${it.scoreB ?: ""}"
        )
    }
    return sb.toString()
}

private fun statusName(status: MatchStatus): String = when (status) {
    MatchStatus.CONFIRMED -> "Nog te spelen"
    MatchStatus.PLAYED -> "Gespeeld"
    MatchStatus.FINISHED -> "Afgerond"
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "TeamCreatorr export")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Exporteren"))
}


