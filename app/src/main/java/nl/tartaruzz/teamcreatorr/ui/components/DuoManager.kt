package nl.tartaruzz.teamcreatorr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames

@Composable
fun DuoManagerSection(
    duos: List<DuoWithNames>,
    players: List<PlayerEntity>,
    onAdd: (Long, Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    Column {
        Text(
            "Duo's (blijven bij elkaar)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Koppel twee spelers die altijd in hetzelfde team moeten zitten.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (duos.isEmpty()) {
            Text(
                "Geen duo's ingesteld.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            duos.forEach { duo ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${duo.playerA.name} + ${duo.playerB.name}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(duo.id) }) {
                        Icon(
                            Icons.Filled.LinkOff,
                            contentDescription = "Ontkoppel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                        "Niet genoeg vrije spelers. Ontkoppel eerst een duo.",
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
