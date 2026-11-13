package nl.tartaruzz.teamcreatorr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

/**
 * Vertical number scroller (no keyboard). The number that sits in the grey
 * centre area is always the selected value. The mapping between the scroll
 * position and the selected value is direct and exact (no lazy-list padding
 * ambiguity), and the list snaps back to the nearest number when scrolling ends.
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 0..30,
    modifier: Modifier = Modifier
) {
    val itemHeight = 44.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val scrollState = rememberScrollState()
    val currentValue by rememberUpdatedState(value)
    var ready by remember { mutableStateOf(false) }

    // Initial position: scroll so the stored value sits in the centre. Wait until
    // the scrollable has a real max range before scrolling.
    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.maxValue }
            .first { it > 0 }
        scrollState.scrollTo((value - range.first) * itemHeightPx.toInt())
        ready = true
    }

    // The number at the centre is derived directly from the scroll offset.
    val selectedIndex =
        if (ready) (scrollState.value / itemHeightPx).roundToInt().coerceIn(0, range.count() - 1)
        else (value - range.first).coerceIn(0, range.count() - 1)

    LaunchedEffect(selectedIndex) {
        val v = range.first + selectedIndex
        if (v != currentValue) onValueChange(v)
    }

    // Snap to the nearest number when scrolling stops (also after slow drags).
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { inProgress ->
                if (!inProgress && ready) {
                    val idx = (scrollState.value / itemHeightPx).roundToInt().coerceIn(0, range.count() - 1)
                    val target = idx * itemHeightPx.toInt()
                    if (scrollState.value != target) scrollState.animateScrollTo(target)
                }
            }
    }

    Box(modifier.height(itemHeight * 5), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = itemHeight * 2)
        ) {
            range.forEach { v ->
                val selected = v == value
                Box(
                    Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$v",
                        style = if (selected) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}



