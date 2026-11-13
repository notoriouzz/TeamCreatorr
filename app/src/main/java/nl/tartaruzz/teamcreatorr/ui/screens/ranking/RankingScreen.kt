package nl.tartaruzz.teamcreatorr.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.tartaruzz.teamcreatorr.R
import nl.tartaruzz.teamcreatorr.domain.FormResult
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.ui.components.AppHeader
import nl.tartaruzz.teamcreatorr.ui.components.PlayerAvatar
import nl.tartaruzz.teamcreatorr.ui.viewmodel.RankingViewModel
import nl.tartaruzz.teamcreatorr.ui.viewmodel.appViewModel

@Composable
fun RankingScreen(onOpenPlayer: (Long) -> Unit) {
    val vm: RankingViewModel = appViewModel()
    val ranking by vm.ranking.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AppHeader(stringResource(R.string.ranking_title))
        Spacer(Modifier.height(12.dp))
        if (ranking.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.ranking_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    stringResource(R.string.ranking_empty_sub),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)) {
                item { TableHeader() }
                items(ranking, key = { it.playerId }) { stats ->
                    TableRow(stats, onClick = { onOpenPlayer(stats.playerId) })
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeadCell(stringResource(R.string.ranking_col_pos), Modifier.width(24.dp))
        Spacer(Modifier.weight(1f))
        HeadCell(stringResource(R.string.ranking_col_p), Modifier.width(22.dp))
        HeadCell(stringResource(R.string.ranking_col_w), Modifier.width(22.dp))
        HeadCell(stringResource(R.string.ranking_col_d), Modifier.width(22.dp))
        HeadCell(stringResource(R.string.ranking_col_l), Modifier.width(22.dp))
        HeadCell(stringResource(R.string.ranking_col_winpct), Modifier.width(36.dp))
        HeadCell(stringResource(R.string.ranking_col_pts), Modifier.width(32.dp))
        HeadCell(stringResource(R.string.ranking_col_form), Modifier.width(46.dp))
    }
}

@Composable
private fun TableRow(stats: PlayerStats, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${stats.rank}",
            Modifier.width(24.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(stats.name, stats.photoFileName, size = 28.dp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    stats.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!stats.active) {
                    Text(
                        stringResource(R.string.players_inactive),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        StatCell("${stats.played}", 22.dp)
        StatCell("${stats.wins}", 22.dp)
        StatCell("${stats.draws}", 22.dp)
        StatCell("${stats.losses}", 22.dp)
        StatCell("${stats.winPercentage}", 36.dp)
        StatCell("${stats.totalPoints}", 32.dp, bold = true, color = MaterialTheme.colorScheme.primary)
        FormDots(stats.form, Modifier.width(46.dp))
    }
}

@Composable
private fun HeadCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StatCell(text: String, width: androidx.compose.ui.unit.Dp, bold: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text,
        Modifier.width(width),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color
    )
}

@Composable
private fun FormDots(form: List<FormResult>, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        if (form.isEmpty()) {
            Text("–", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            form.forEach { r ->
                val color = when (r) {
                    FormResult.WIN -> Color(0xFF43A047)
                    FormResult.DRAW -> Color(0xFF9E9E9E)
                    FormResult.LOSS -> Color(0xFFE53935)
                }
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            }
        }
    }
}

