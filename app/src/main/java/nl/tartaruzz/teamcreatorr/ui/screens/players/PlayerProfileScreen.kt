package nl.tartaruzz.teamcreatorr.ui.screens.players

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.tartaruzz.teamcreatorr.domain.ManualAdjustment
import nl.tartaruzz.teamcreatorr.domain.PlayerHistoryItem
import nl.tartaruzz.teamcreatorr.domain.PlayerProfileUiData
import nl.tartaruzz.teamcreatorr.domain.PointHistoryEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.PlayerAvatar
import nl.tartaruzz.teamcreatorr.ui.components.SectionTitle
import nl.tartaruzz.teamcreatorr.ui.theme.TeamAColor
import nl.tartaruzz.teamcreatorr.ui.theme.TeamBColor
import nl.tartaruzz.teamcreatorr.ui.viewmodel.PlayerProfileViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Formatters
import nl.tartaruzz.teamcreatorr.util.PhotoStore
import java.io.File

private enum class HistoryFilter(val label: String) {
    ALL("Alle"), WON("Gewonnen"), LOST("Verloren"), DRAW("Gelijk")
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }

    var showEdit by remember { mutableStateOf(false) }
    var showAddAdjustment by remember { mutableStateOf(false) }
    var editingAdjustment by remember { mutableStateOf<ManualAdjustment?>(null) }
    var showBigAvatar by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            scope.launch {
                val fileName = try {
                    withContext(Dispatchers.IO) { PhotoStore.save(context, it, playerId) }
                } catch (e: Exception) {
                    null
                }
                if (fileName != null) vm.updatePhoto(playerId, fileName)
                else Toast.makeText(context, "Foto kon niet worden geladen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                AppHeader(profile?.name ?: stringResource(R.string.players_title), onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(
                    onClick = { showEdit = true },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit), tint = androidx.compose.ui.graphics.Color.White)
                }
            }
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
                item {
                    ProfileHeader(
                        data = data,
                        onOpenPhoto = { showBigAvatar = true }
                    )
                }

                item {
                    SectionTitle("Puntenopbouw")
                    PointsSection(
                        entries = data.pointHistory,
                        onAdd = { showAddAdjustment = true },
                        onEdit = { id -> editingAdjustment = data.manualAdjustments.firstOrNull { it.id == id } },
                        onDelete = { id -> vm.deleteAdjustment(id) }
                    )
                }

                item {
                    SectionTitle("Wedstrijdgeschiedenis")
                    FilterChips(filter) { filter = it }
                }
                val filtered = filteredHistory(data.history, filter)
                if (filtered.isEmpty()) {
                    item {
                        Text("Geen wedstrijden voor dit filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(filtered, key = { it.matchId }) { item ->
                        HistoryItemCard(item, onClick = { onOpenMatch(item.matchId) })
                    }
                }
            }
        }
    }

    if (showBigAvatar && profile != null) {
        Dialog(onDismissRequest = { showBigAvatar = false }) {
            Box(
                Modifier.fillMaxSize().clickable { showBigAvatar = false },
                contentAlignment = Alignment.Center
            ) {
                PlayerAvatar(profile!!.name, profile!!.photoFileName, size = 220.dp, borderWidth = 4.dp)
            }
        }
    }

    if (showEdit && profile != null) {
        var name by remember(profile!!.playerId) { mutableStateOf(profile!!.name) }
        var nickname by remember(profile!!.playerId) { mutableStateOf(profile!!.nickname ?: "") }
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text(stringResource(R.string.players_edit_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Naam") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname (optioneel)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    TextButton(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.profile_choose_photo))
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    vm.updatePlayer(profile!!.playerId, name, nickname.trim().ifEmpty { null })
                    showEdit = false
                }) { Text("Opslaan") }
            },
            dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Annuleren") } }
        )
    }

    if (showAddAdjustment && profile != null) {
        var points by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAdjustment = false },
            title = { Text("Punten toevoegen / aftrekken") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it },
                        label = { Text("Aantal punten (bijv. 5 of -3)") },
                        singleLine = true
                    )
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Omschrijving (verplicht)") })
                }
            },
            confirmButton = {
                TextButton(enabled = points.toIntOrNull() != null && desc.isNotBlank(), onClick = {
                    vm.addAdjustment(profile!!.playerId, points.toIntOrNull() ?: 0, desc)
                    showAddAdjustment = false
                }) { Text("Opslaan") }
            },
            dismissButton = { TextButton(onClick = { showAddAdjustment = false }) { Text("Annuleren") } }
        )
    }

    editingAdjustment?.let { adj ->
        var points by remember(adj.id) { mutableStateOf(adj.points.toString()) }
        var desc by remember(adj.id) { mutableStateOf(adj.description) }
        AlertDialog(
            onDismissRequest = { editingAdjustment = null },
            title = { Text("Puntmutatie aanpassen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it },
                        label = { Text("Aantal punten") },
                        singleLine = true
                    )
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Omschrijving") })
                }
            },
            confirmButton = {
                TextButton(enabled = points.toIntOrNull() != null && desc.isNotBlank(), onClick = {
                    vm.updateAdjustment(adj.id, points.toIntOrNull() ?: 0, desc)
                    editingAdjustment = null
                }) { Text("Opslaan") }
            },
            dismissButton = { TextButton(onClick = { editingAdjustment = null }) { Text("Annuleren") } }
        )
    }
}

@Composable
private fun ProfileHeader(
    data: PlayerProfileUiData,
    onOpenPhoto: () -> Unit
) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PlayerAvatar(data.name, data.photoFileName, size = 72.dp, modifier = Modifier.clickable { onOpenPhoto() })
                Column(Modifier.weight(1f)) {
                    Text(data.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (!data.nickname.isNullOrBlank()) {
                        Text("\"${data.nickname}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!data.active) Text("Inactief", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    val rank = data.stats?.rank
                    if (rank != null) {
                        Text("Positie: #$rank", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Rating: ${data.rating}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
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
                HeaderStat(stringResource(R.string.profile_played), "${data.stats?.played ?: 0}", Modifier.weight(1f))
                HeaderStat(stringResource(R.string.profile_wins), "${data.stats?.wins ?: 0}", Modifier.weight(1f))
                HeaderStat(stringResource(R.string.profile_losses), "${data.stats?.losses ?: 0}", Modifier.weight(1f))
                HeaderStat(stringResource(R.string.profile_draws), "${data.stats?.draws ?: 0}", Modifier.weight(1f))
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
            Spacer(Modifier.height(12.dp))
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
private fun PointsSection(
    entries: List<PointHistoryEntry>,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text("Punten toevoegen / aftrekken")
        }
        if (entries.isEmpty()) {
            Text("Nog geen punten.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            entries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            Formatters.formatDate(entry.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            entry.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (entry.isManual) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    Text(
                        signed(entry.points),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.points >= 0) Color(0xFF43A047) else Color(0xFFE53935)
                    )
                    if (entry.isManual && entry.adjustmentId != null) {
                        IconButton(onClick = { onEdit(entry.adjustmentId!!) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Bewerken", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDelete(entry.adjustmentId!!) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Verwijderen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun signed(value: Int) = if (value >= 0) "+$value" else "$value"

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
private fun annotatedScore(item: PlayerHistoryItem): AnnotatedString {
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



