package nl.tartaruzz.teamcreatorr.domain

import kotlin.math.roundToInt

/**
 * Calculates a player rating between 1 and 99, based only on the average
 * number of automatically earned match points per played match.
 * Manual point adjustments and not-yet-played matches do NOT influence the rating.
 */
object RatingCalculator {

    const val NEUTRAL_RATING = 50

    /**
     * @param played     number of matches the player actually played (with a result)
     * @param autoPoints total automatically earned match points (attendance + win + draw)
     * @param settings   current point settings
     */
    fun rating(played: Int, autoPoints: Int, settings: PointSettings): Int {
        if (played <= 0) return NEUTRAL_RATING
        val maxPerMatch = settings.attendancePoints + maxOf(settings.winPoints, settings.drawPoints)
        if (maxPerMatch <= 0) return NEUTRAL_RATING
        val average = autoPoints.toDouble() / played
        return (1 + 98 * (average / maxPerMatch)).roundToInt().coerceIn(1, 99)
    }
}
