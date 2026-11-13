package nl.tartaruzz.teamcreatorr.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Central language preference + locale application for pre-appcompat i18n. */
object LanguagePrefs {
    private const val PREFS = "teamcreatorr_prefs"
    private const val KEY_LANG = "language"

    const val DEFAULT = "nl"
    val SUPPORTED = listOf("nl", "en", "tr")

    fun get(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, DEFAULT) ?: DEFAULT

    fun set(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
    }

    fun applyLocale(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
