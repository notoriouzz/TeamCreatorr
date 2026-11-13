package nl.tartaruzz.teamcreatorr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Vertical number scroller (no keyboard). The selected value is visually
 * highlighted in the centre of the picker. The selected value is always derived
 * from the item that is actually closest to the centre, so the highlight box and
 * the stored value can never drift apart.
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
    // Centre of the 5-item tall box (in px), measured from the top of the list.
    val centerPx = itemHeightPx * 2.5f
    val listState = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    // Position the current value at the centre once, on first composition.
    LaunchedEffect(Unit) {
        listState.scrollToItem((value - range.first).coerceAtLeast(0))
    }

    // Keep the selected value in sync with whichever item sits at the centre.
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { items ->
                val centerItem = items.minByOrNull { abs(it.offset + it.size / 2f - centerPx) }
                if (centerItem != null) {
                    val v = range.first + centerItem.index
                    if (v in range && v != value) onValueChange(v)
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
        LazyColumn(
            state = listState,
            flingBehavior = fling,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * 2)
        ) {
            items(range.count()) { i ->
                val v = range.first + i
                val selected = v == value
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { onValueChange(v) },
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

