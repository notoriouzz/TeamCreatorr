package nl.tartaruzz.teamcreatorr.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.tartaruzz.teamcreatorr.util.PhotoStore

/**
 * Shared avatar with a small white ring, used everywhere in the app so avatars
 * look consistent. Falls back to the player's initial when no photo exists.
 */
@Composable
fun PlayerAvatar(
    name: String,
    photoFileName: String?,
    size: Dp = 48.dp,
    borderWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by rememberAvatar(context, photoFileName)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(Modifier.matchParentSize().clip(CircleShape).background(Color.White))
        Box(
            Modifier
                .fillMaxSize()
                .padding(borderWidth)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Profielfoto van $name",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun rememberAvatar(context: Context, photoFileName: String?): State<Bitmap?> =
    produceState<Bitmap?>(initialValue = null, photoFileName) {
        value = if (photoFileName.isNullOrBlank()) null
        else withContext(Dispatchers.IO) { PhotoStore.loadAvatar(context, photoFileName) }
    }

/** Pulsing placeholder used while data is still loading. */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(14.dp)) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}

@Composable
fun SkeletonCircle(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    SkeletonBox(modifier = modifier.size(size), shape = CircleShape)
}
