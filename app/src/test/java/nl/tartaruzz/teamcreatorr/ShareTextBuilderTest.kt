package nl.tartaruzz.teamcreatorr

import nl.tartaruzz.teamcreatorr.domain.PlayerRef
import nl.tartaruzz.teamcreatorr.domain.ShareTextBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareTextBuilderTest {

    @Test
    fun `team split is formatted with names and nicknames`() {
        val teamA = listOf(
            PlayerRef(1, "Jan", "De Tank"),
            PlayerRef(2, "Piet", null),
            PlayerRef(3, "Kees", "Speedy")
        )
        val teamB = listOf(
            PlayerRef(4, "Wim", null),
            PlayerRef(5, "Tom", "Keeper"),
            PlayerRef(6, "Sam", null)
        )
        val text = ShareTextBuilder.teamIndeling(teamA, teamB)

        assertTrue(text.contains("TEAM A"))
        assertTrue(text.contains("TEAM B"))
        assertTrue(text.contains("1. Jan — \"De Tank\""))
        assertTrue(text.contains("2. Piet"))
        assertTrue(text.contains("3. Kees — \"Speedy\""))
        assertTrue(text.contains("2. Tom — \"Keeper\""))
        assertTrue(text.contains("3. Sam"))
        assertFalse(text.contains("\"\""))
    }

    @Test
    fun `team split works for 11v11 numbering`() {
        val teamA = (1L..11L).map { PlayerRef(it, "Speler $it", null) }
        val teamB = (12L..22L).map { PlayerRef(it, "Speler $it", null) }
        val text = ShareTextBuilder.teamIndeling(teamA, teamB)
        assertTrue(text.contains("11. Speler 11"))
        assertTrue(text.contains("1. Speler 12"))
    }
}
