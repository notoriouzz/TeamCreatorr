package nl.tartaruzz.teamcreatorr.ui.screens.match

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.MatchStatus
import nl.tartaruzz.teamcreatorr.domain.ShareTextBuilder
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.NumberPicker
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.components.TeamCard
import nl.tartaruzz.teamcreatorr.ui.components.statusColor
import nl.tartaruzz.teamcreatorr.ui.components.statusLabel
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.MatchDetailViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters

@Composable
fun MatchDetailScreen(matchId: Long, onBack: () -> Unit) {
    val vm: MatchDetailViewModel = appViewModel()
    LaunchedEffect(matchId) { vm.load(matchId) }
    val match by vm.match.collectAsState()
    val context = LocalContext.current

    var editingResult by remember(matchId) { mutableStateOf(false) }
    var scoreA by remember(matchId) { mutableIntStateOf(0) }
    var scoreB by remember(matchId) { mutableIntStateOf(0) }
    var initialized by remember(matchId) { mutableStateOf(false) }
    var showDelete by remember(matchId) { mutableStateOf(false) }

    LaunchedEffect(match) {
        val m = match
        if (m != null && !initialized) {
            scoreA = m.scoreA ?: 0
            scoreB = m.scoreB ?: 0
            editingResult = m.status == MatchStatus.PLAYED
            initialized = true
        }
    }
    LaunchedEffect(match?.status) {
        val m = match ?: return@LaunchedEffect
        if (m.status == MatchStatus.PLAYED && !editingResult) {
            scoreA = 0
            scoreB = 0
            editingResult = true
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AppHeader(stringResource(R.string.match_detail_title), onBack = onBack)
        val m = match
        if (m == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(Formatters.formatLongDate(m.date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.match_vs, m.teamSize, m.teamSize), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Text(statusLabel(m.status), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = statusColor(m.status))
                        }
                    }
                }
                item { SectionTitle(stringResource(R.string.match_detail_teams)) }
                item { TeamCard(stringResource(R.string.new_match_team_a), TeamAColor, m.teamA) }
                item { TeamCard(stringResource(R.string.new_match_team_b), TeamBColor, m.teamB) }

                when {
                    m.status == MatchStatus.CONFIRMED -> item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(stringResource(R.string.match_detail_not_played_text), style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = { vm.markPlayed(m.id) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.match_detail_add_result))
                                }
                            }
                        }
                    }
                    m.status == MatchStatus.PLAYED -> item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(stringResource(R.string.match_detail_add_result), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ScorePicker(stringResource(R.string.match_detail_score_a), scoreA, { scoreA = it }, Modifier.weight(1f))
                                    ScorePicker(stringResource(R.string.match_detail_score_b), scoreB, { scoreB = it }, Modifier.weight(1f))
                                }
                                Button(
                                    onClick = {
                                        vm.saveResult(m.id, scoreA, scoreB)
                                        editingResult = false
                                        Toast.makeText(context, context.getString(R.string.match_detail_saved), Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(6.dp))
                                    Text(stringResource(R.string.match_detail_save_result))
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { shareTeams(context, m) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.match_detail_share))
                        }
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.match_detail_delete))
                        }
                    }
                }
            }
        }
    }

    if (showDelete && match != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.match_detail_delete_title)) },
            text = { Text(stringResource(if (match!!.status == MatchStatus.FINISHED) R.string.match_detail_delete_finished else R.string.match_detail_delete_unplayed)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(match!!.id)
                    showDelete = false
                    onBack()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ScorePicker(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        NumberPicker(value = value, onValueChange = onValueChange, range = 0..30, modifier = Modifier.fillMaxWidth())
    }
}

private fun shareTeams(context: android.content.Context, match: MatchDetail) {
    val text = ShareTextBuilder.teamIndeling(match.teamA, match.teamB)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.match_detail_share)))
}
