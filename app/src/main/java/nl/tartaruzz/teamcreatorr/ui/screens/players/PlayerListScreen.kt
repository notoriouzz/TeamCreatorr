package nl.tartaruzz.teamcreatorr.ui.screens.players

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.PlayerAvatar
import nl.tartaruzz.teamcreatorr.ui.viewmodel.PlayerListViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel
import nl.tartaruzz.teamcreatorr.util.Toasts

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerListScreen(onOpenPlayer: (Long) -> Unit) {
    val vm: PlayerListViewModel = appViewModel()
    val players by vm.players.collectAsState()
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var showBulk by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteSelection by remember { mutableStateOf(false) }

    val selectionMode = selectedIds.isNotEmpty()

    fun toggle(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Scaffold(
        topBar = {
            Box {
                AppHeader(stringResource(R.string.players_title), modifier = Modifier.padding(horizontal = 16.dp))
                if (selectionMode) {
                    IconButton(onClick = { showDeleteSelection = true }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White)
                    }
                } else {
                    IconButton(onClick = { showBulk = true }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = stringResource(R.string.players_bulk_title), tint = Color.White)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.players_add_title))
            }
        }
    ) { padding ->
        if (players.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.players_empty),
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
                        selected = player.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = { if (selectionMode) toggle(player.id) else onOpenPlayer(player.id) },
                        onLongClick = { toggle(player.id) }
                    )
                }
            }
        }
    }

    if (showAdd) {
        PlayerDialog(
            title = stringResource(R.string.players_add_title),
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
                Toasts.show(context, R.string.players_added)
            }
        )
    }

    editing?.let { player ->
        PlayerDialog(
            title = stringResource(R.string.players_edit_title),
            initialName = player.name,
            initialNickname = player.nickname ?: "",
            onDismiss = { editing = null },
            onConfirm = { name, nickname ->
                vm.update(player.id, name, nickname)
                editing = null
            }
        )
    }

    if (showDeleteSelection) {
        AlertDialog(
            onDismissRequest = { showDeleteSelection = false },
            title = { Text(stringResource(R.string.players_delete_title)) },
            text = { Text(stringResource(R.string.players_delete_many, selectedIds.size), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteMany(selectedIds.toList()) {
                        Toasts.show(context, R.string.players_deleted)
                    }
                    selectedIds = emptySet()
                    showDeleteSelection = false
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteSelection = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerRow(
    player: PlayerEntity,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
                Spacer(Modifier.width(4.dp))
            }
            PlayerAvatar(player.name, player.photoFileName, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (player.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!player.nickname.isNullOrBlank()) {
                    Text(
                        "\"${player.nickname}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!player.active) {
                    Text(
                        stringResource(R.string.players_inactive),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.players_name)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.players_nickname)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), nickname.trim().ifEmpty { null }) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
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
        title = { Text(stringResource(R.string.players_bulk_title)) },
        text = {
            Column {
                Text(stringResource(R.string.players_bulk_hint), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text(stringResource(R.string.players_names_label)) }
                )
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.lines()) }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

