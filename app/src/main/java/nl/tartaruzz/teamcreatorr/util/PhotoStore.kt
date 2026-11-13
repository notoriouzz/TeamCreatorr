package nl.tartaruzz.teamcreatorr.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File

/**
 * Stores optimized player photos on internal storage and keeps only a file name
 * in the database. Handles downscaling, compression and EXIF orientation.
 */
object PhotoStore {

    private const val MAX_DIM = 640
    private const val QUALITY = 85

    fun dir(context: Context): File = File(context.filesDir, "photos").apply { mkdirs() }

    fun fileFor(context: Context, fileName: String): File = File(dir(context), fileName)

    fun load(context: Context, fileName: String?): File? {
        if (fileName.isNullOrBlank()) return null
        val f = fileFor(context, fileName)
        return if (f.exists()) f else null
    }

    private const val AVATAR_MAX = 256
    private val avatarCache = LruCache<String, Bitmap>(24)

    /**
     * Loads a downsampled, in-memory cached avatar bitmap. Call from a background
     * dispatcher; the cache avoids repeated decode work for the same photo.
     */
    fun loadAvatar(context: Context, fileName: String?): Bitmap? {
        if (fileName.isNullOrBlank()) return null
        avatarCache.get(fileName)?.let { return it }
        val f = fileFor(context, fileName)
        if (!f.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= AVATAR_MAX &&
            bounds.outHeight / (sample * 2) >= AVATAR_MAX
        ) {
            sample *= 2
        }
        val bmp = BitmapFactory.decodeFile(
            f.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample }
        )
        if (bmp != null) avatarCache.put(fileName, bmp)
        return bmp
    }

    fun save(context: Context, uri: Uri, playerId: Long): String {
        val bitmap = decode(context, uri) ?: throw IllegalStateException("Afbeelding kon niet worden gelezen")
        val scaled = scale(bitmap, MAX_DIM)
        try {
            val file = File(dir(context), "p$playerId.jpg")
            file.outputStream().use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out) }
            return file.name
        } finally {
            if (scaled !== bitmap) bitmap.recycle()
            scaled.recycle()
        }
    }

    fun delete(context: Context, fileName: String?) {
        if (fileName.isNullOrBlank()) return
        fileFor(context, fileName).delete()
    }

    private fun decode(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val orientation = runCatching {
                ExifInterface(ByteArrayInputStream(bytes))
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            rotate(bitmap, orientation)
        }
    }

    private fun rotate(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    private fun scale(src: Bitmap, maxDim: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxDim && h <= maxDim) return src
        val factor = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        val nw = (w * factor).toInt().coerceAtLeast(1)
        val nh = (h * factor).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}
