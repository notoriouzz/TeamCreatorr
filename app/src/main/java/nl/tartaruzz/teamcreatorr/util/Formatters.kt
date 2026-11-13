package nl.tartaruzz.teamcreatorr.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatDate(epochMillis: Long): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(epochMillis))

    fun formatLongDate(epochMillis: Long): String =
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.forLanguageTag("nl-NL")).format(Date(epochMillis))
}
