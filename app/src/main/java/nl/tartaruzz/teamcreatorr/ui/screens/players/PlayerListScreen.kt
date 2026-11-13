package nl.tartaruzz.teamcreatorr.ui.screens.players

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.ui.viewmodel.PlayerListViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    onOpenPlayer: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: PlayerListViewModel = appViewModel()
    val players by vm.players.collectAsState()
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var showBulk by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlayerEntity?>(null) }
    var deleting by remember { mutableStateOf<PlayerEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spelers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Terug")
                    }
                },
                actions = {
                    IconButton(onClick = { showBulk = true }) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = "Meerdere spelers toevoegen")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Speler toevoegen")
            }
        }
    ) { padding ->
        if (players.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Nog geen spelers. Tik op + om spelers toe te voegen.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerRow(
                        player = player,
                        onClick = { onOpenPlayer(player.id) },
                        onEdit = { editing = player },
                        onDelete = { deleting = player },
                        onToggleActive = { vm.setActive(player.id, it) }
                    )
                }
            }
        }
    }

    if (showAdd) {
        PlayerDialog(
            title = "Speler toevoegen",
            initialName = "",
            initialNickname = "",
            onDismiss = { showAdd = false },
            onConfirm = { name, nickname ->
                vm.add(name, nickname)
                showAdd = false
            }
        )
    }

    if (showBulk) {
        BulkAddDialog(
            onDismiss = { showBulk = false },
            onConfirm = { names ->
                vm.addMany(names)
                showBulk = false
                Toast.makeText(context, "Spelers toegevoegd", Toast.LENGTH_SHORT).show()
            }
        )
    }

    editing?.let { player ->
        PlayerDialog(
            title = "Speler aanpassen",
            initialName = player.name,
            initialNickname = player.nickname ?: "",
            onDismiss = { editing = null },
            onConfirm = { name, nickname ->
                vm.update(player.id, name, nickname)
                editing = null
            }
        )
    }

    deleting?.let { player ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Speler verwijderen") },
            text = {
                Text(
                    "Weet je zeker dat je '${player.name}' wilt verwijderen? " +
                        "Als deze speler al wedstrijden heeft gespeeld, wordt hij uitgeschakeld " +
                        "zodat de historie behouden blijft."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(player.id) { deleted ->
                        val msg = if (deleted) "Speler verwijderd" else "Speler uitgeschakeld (historie behouden)"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    deleting = null
                }) { Text("Verwijderen") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Annuleren") }
            }
        )
    }
}

@Composable
private fun PlayerRow(
    player: PlayerEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = if (player.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (player.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!player.active) {
                    Text(
                        "Inactief",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Bewerken")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Verwijderen", tint = MaterialTheme.colorScheme.error)
            }
            Switch(checked = player.active, onCheckedChange = onToggleActive)
        }
    }
}

@Composable
private fun PlayerDialog(
    title: String,
    initialName: String,
    initialNickname: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var nickname by remember(initialNickname) { mutableStateOf(initialNickname) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Naam") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    singleLine = true,
                    label = { Text("Nickname (optioneel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), nickname.trim().ifEmpty { null }) }) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleren") }
        }
    )
}

@Composable
private fun BulkAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Meerdere spelers toevoegen") },
        text = {
            Column {
                Text("Voer per regel één naam in.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("Namen") }
                )
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.lines()) }) {
                Text("Toevoegen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleren") }
        }
    )
}
