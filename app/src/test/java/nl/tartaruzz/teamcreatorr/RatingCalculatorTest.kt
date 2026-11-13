package nl.tartaruzz.teamcreatorr

import nl.tartaruzz.teamcreatorr.domain.PointSettings
import nl.tartaruzz.teamcreatorr.domain.RatingCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingCalculatorTest {

    private val settings = PointSettings(1, 2, 1)

    @Test
    fun `player without matches gets neutral rating`() {
        assertEquals(RatingCalculator.NEUTRAL_RATING, RatingCalculator.rating(0, 0, settings))
    }

    @Test
    fun `always winning player gets high rating`() {
        val rating = RatingCalculator.rating(10, 30, settings) // avg 3 = attendance 1 + win 2
        assertTrue(rating >= 90)
    }

    @Test
    fun `always drawing player gets mid rating`() {
        val rating = RatingCalculator.rating(10, 20, settings) // avg 2 = attendance 1 + draw 1
        assertTrue(rating in 60..75)
    }

    @Test
    fun `rating is between 1 and 99`() {
        for (played in 1..20) {
            for (autoPoints in 0..(played * 5)) {
                val r = RatingCalculator.rating(played, autoPoints, settings)
                assertTrue("rating $r out of range", r in 1..99)
            }
        }
    }

    @Test
    fun `many played matches do not inflate rating versus better average`() {
        // Player A: 20 matches, 1 point per match (attendance only) -> low average
        // Player B: 5 matches, 3 points per match (always wins) -> high average
        val a = RatingCalculator.rating(20, 20, settings)
        val b = RatingCalculator.rating(5, 15, settings)
        assertTrue("Expected $b > $a", b > a)
    }
}
