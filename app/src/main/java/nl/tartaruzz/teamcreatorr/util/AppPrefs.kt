package nl.tartaruzz.teamcreatorr.util

import android.content.Context

/**
 * Lightweight UI preferences (theme + toast behaviour) stored in SharedPreferences.
 * These are intentionally kept separate from the Room `settings` table because they
 * must be available before the database is initialised (e.g. to choose the theme).
 */
object AppPrefs {
    private const val PREFS = "teamcreatorr_ui_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_TOAST_ENABLED = "toast_enabled"
    private const val KEY_TOAST_POSITION = "toast_position"

    const val DARK_MODE_DEFAULT = false
    const val TOAST_ENABLED_DEFAULT = true
    const val TOAST_POSITION_DEFAULT = "top"

    fun darkModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, DARK_MODE_DEFAULT)

    fun setDarkMode(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun toastEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TOAST_ENABLED, TOAST_ENABLED_DEFAULT)

    fun setToastEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_TOAST_ENABLED, enabled).apply()

    fun toastPosition(context: Context): String =
        prefs(context).getString(KEY_TOAST_POSITION, TOAST_POSITION_DEFAULT) ?: TOAST_POSITION_DEFAULT

    fun setToastPosition(context: Context, position: String) =
        prefs(context).edit().putString(KEY_TOAST_POSITION, position).apply()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
