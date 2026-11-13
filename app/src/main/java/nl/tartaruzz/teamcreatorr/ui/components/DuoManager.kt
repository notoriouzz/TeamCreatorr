package nl.tartaruzz.teamcreatorr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames

/**
 * Duo management for the match screen only. Saved duos are shown with a
 * per-duo on/off switch; disabled duos are ignored for the current match.
 */
@Composable
fun DuoMatchManager(
    duos: List<DuoWithNames>,
    players: List<PlayerEntity>,
    disabledDuoIds: Set<Long>,
    onToggleDuo: (Long) -> Unit,
    onAdd: (Long, Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    Column {
        Text(
            "Duo's",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Spelers die altijd bij elkaar in hetzelfde team moeten zitten. Uitgeschakelde duo's doen niet mee.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (duos.isEmpty()) {
            Text(
                "Nog geen duo's ingesteld.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            duos.forEach { duo ->
                DuoRow(
                    duo = duo,
                    enabled = duo.id !in disabledDuoIds,
                    onToggle = { onToggleDuo(duo.id) },
                    onRemove = { onRemove(duo.id) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { showAdd = true }, enabled = players.size >= 2) {
            Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Duo toevoegen")
        }
    }

    if (showAdd) {
        AddDuoDialog(
            players = players,
            existingDuos = duos,
            onDismiss = { showAdd = false },
            onConfirm = { a, b ->
                onAdd(a, b)
                showAdd = false
            }
        )
    }
}

@Composable
private fun DuoRow(
    duo: DuoWithNames,
    enabled: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Link,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${duo.playerA.name} + ${duo.playerB.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    if (enabled) "Actief" else "Uitgeschakeld",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Duo verwijderen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddDuoDialog(
    players: List<PlayerEntity>,
    existingDuos: List<DuoWithNames>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var a by remember { mutableStateOf<PlayerEntity?>(null) }
    var b by remember { mutableStateOf<PlayerEntity?>(null) }
    val usedIds = remember(existingDuos) {
        existingDuos.flatMap { listOf(it.playerA.id, it.playerB.id) }.toSet()
    }
    val available = players.filter { it.id !in usedIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duo toevoegen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerDropdown("Speler 1", available, a) { a = it }
                PlayerDropdown("Speler 2", available, b) { b = it }
                if (available.size < 2) {
                    Text(
                        "Niet genoeg vrije spelers. Verwijder eerst een duo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
private fun PlayerDropdown(
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
