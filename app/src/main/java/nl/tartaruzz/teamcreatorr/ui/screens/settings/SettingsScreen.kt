package nl.tartaruzz.teamcreatorr.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import nl.tartaruzz.teamcreatorr.data.Backup
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.NumberPicker
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.viewmodel.SettingsViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.LanguagePrefs

private data class ImportPreview(val json: String, val players: Int, val matches: Int, val duos: Int)
private enum class ResetKind { ALL, MATCHES, PLAYERS, RANKING }

@Composable
fun SettingsScreen() {
    val vm: SettingsViewModel = appViewModel()
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current

    var att by remember { mutableStateOf(1) }
    var win by remember { mutableStateOf(2) }
    var draw by remember { mutableStateOf(1) }
    var initialized by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var resetKind by remember { mutableStateOf<ResetKind?>(null) }
    var importPreview by remember { mutableStateOf<ImportPreview?>(null) }

    LaunchedEffect(settings) {
        if (!initialized) {
            att = settings.attendancePoints
            win = settings.winPoints
            draw = settings.drawPoints
            initialized = true
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            vm.exportBackup { json ->
                val ok = if (json != null) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                        true
                    }.getOrDefault(false)
                } else false
                Toast.makeText(context, context.getString(if (ok) R.string.export_success else R.string.export_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            }.getOrNull()
            val parsed = json?.let { Backup.parse(it) }
            if (parsed == null) {
                Toast.makeText(context, context.getString(R.string.import_invalid), Toast.LENGTH_SHORT).show()
            } else {
                importPreview = ImportPreview(json!!, parsed.players.size, parsed.matches.size, parsed.duos.size)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AppHeader(stringResource(R.string.settings_title))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionTitle(stringResource(R.string.settings_language))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_language_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "nl" to R.string.lang_dutch,
                                "en" to R.string.lang_english,
                                "tr" to R.string.lang_turkish
                            ).forEach { (code, labelRes) ->
                                FilterChip(
                                    selected = LanguagePrefs.get(context) == code,
                                    onClick = {
                                        LanguagePrefs.set(context, code)
                                        (context as? Activity)?.recreate()
                                    },
                                    label = { Text(stringResource(labelRes)) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.settings_points))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PickerWithLabel(stringResource(R.string.settings_attendance), att, { att = it }, Modifier.weight(1f))
                            PickerWithLabel(stringResource(R.string.settings_win), win, { win = it }, Modifier.weight(1f))
                            PickerWithLabel(stringResource(R.string.settings_draw), draw, { draw = it }, Modifier.weight(1f))
                        }
                        Text(stringResource(R.string.settings_points_recalc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { showConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_points_update))
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.settings_data))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { exportLauncher.launch("teamcreatorr-backup.json") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.settings_export))
                        }
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_import))
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.settings_donate_title))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.settings_donate_text), style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { openKofi(context) }, modifier = Modifier.fillMaxWidth()) {
                            Text("☕")
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.settings_donate_button), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.settings_reset))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_reset_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = { resetKind = ResetKind.ALL }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_reset_all), color = MaterialTheme.colorScheme.error)
                        }
                        OutlinedButton(onClick = { resetKind = ResetKind.MATCHES }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_reset_matches))
                        }
                        OutlinedButton(onClick = { resetKind = ResetKind.PLAYERS }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_reset_players))
                        }
                        OutlinedButton(onClick = { resetKind = ResetKind.RANKING }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.settings_reset_ranking))
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.settings_about))
                Text(stringResource(R.string.settings_about_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.settings_apply_title)) },
            text = { Text(stringResource(R.string.settings_apply_msg, att, win, draw)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateSettings(att, win, draw)
                    showConfirm = false
                    Toast.makeText(context, context.getString(R.string.settings_points_update), Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.settings_apply)) }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    resetKind?.let { kind ->
        val msgRes = when (kind) {
            ResetKind.ALL -> R.string.reset_confirm_all
            ResetKind.MATCHES -> R.string.reset_confirm_matches
            ResetKind.PLAYERS -> R.string.reset_confirm_players
            ResetKind.RANKING -> R.string.reset_confirm_ranking
        }
        AlertDialog(
            onDismissRequest = { resetKind = null },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(msgRes)) },
            confirmButton = {
                TextButton(onClick = {
                    val done = { Toast.makeText(context, context.getString(R.string.reset_done), Toast.LENGTH_SHORT).show() }
                    when (kind) {
                        ResetKind.ALL -> vm.resetAll(done)
                        ResetKind.MATCHES -> vm.resetMatches(done)
                        ResetKind.PLAYERS -> vm.resetPlayers(done)
                        ResetKind.RANKING -> vm.resetRanking(done)
                    }
                    resetKind = null
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { resetKind = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    importPreview?.let { p ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text(stringResource(R.string.import_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.import_preview, p.players, p.matches, p.duos))
                    Text(stringResource(R.string.import_confirm_msg))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.importBackup(p.json, replace = true) { result ->
                        Toast.makeText(context, context.getString(if (result is Repository.ImportResult.Success) R.string.import_success else R.string.import_failed), Toast.LENGTH_SHORT).show()
                    }
                    importPreview = null
                }) { Text(stringResource(R.string.import_mode_replace)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.importBackup(p.json, replace = false) { result ->
                        Toast.makeText(context, context.getString(if (result is Repository.ImportResult.Success) R.string.import_success else R.string.import_failed), Toast.LENGTH_SHORT).show()
                    }
                    importPreview = null
                }) { Text(stringResource(R.string.import_mode_merge)) }
            }
        )
    }
}

@Composable
private fun PickerWithLabel(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        NumberPicker(value = value, onValueChange = onValueChange, range = 0..10, modifier = Modifier.fillMaxWidth())
    }
}

private fun openKofi(context: android.content.Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/tartaruzz")))
}
