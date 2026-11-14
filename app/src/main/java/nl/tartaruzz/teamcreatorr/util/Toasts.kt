package nl.tartaruzz.teamcreatorr.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes

/** Where a toast is shown on screen. */
enum class ToastPosition(val key: String) {
    TOP("top"),
    CENTER("center"),
    BOTTOM("bottom");

    companion object {
        fun fromKey(key: String): ToastPosition = entries.firstOrNull { it.key == key } ?: TOP
    }
}

/**
 * Single place that renders every toast in the app. Respects the user's
 * on/off, position and (short) duration preferences. Disabled toasts are simply
 * skipped so the underlying action always keeps working.
 */
object Toasts {

    fun show(context: Context, @StringRes messageRes: Int) = show(context, context.getString(messageRes))

    fun show(context: Context, message: String) {
        if (!AppPrefs.toastEnabled(context)) return

        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        val position = ToastPosition.fromKey(AppPrefs.toastPosition(context))
        when (position) {
            ToastPosition.TOP -> toast.setGravity(
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                0,
                statusBarHeight(context) + dp(context, 12)
            )
            ToastPosition.CENTER -> toast.setGravity(Gravity.CENTER, 0, 0)
            ToastPosition.BOTTOM -> toast.setGravity(
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                0,
                navigationBarHeight(context) + dp(context, 48)
            )
        }
        toast.show()
    }

    private fun statusBarHeight(context: Context): Int {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else dp(context, 24)
    }

    private fun navigationBarHeight(context: Context): Int {
        val resId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else dp(context, 48)
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
