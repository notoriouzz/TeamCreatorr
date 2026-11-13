package nl.tartaruzz.teamcreatorr

import nl.tartaruzz.teamcreatorr.domain.TeamGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamGeneratorTest {

    private fun players(n: Int): List<Long> = (1L..n.toLong()).toList()

    @Test
    fun `teams have correct sizes for 3v3 up to 11v11`() {
        for (size in 3..11) {
            val result = TeamGenerator.generate(players(size * 2), size, emptyList())
            assertTrue("Expected success for $size tegen $size", result is TeamGenerator.Result.Success)
            val split = (result as TeamGenerator.Result.Success).split
            assertEquals(size, split.teamA.size)
            assertEquals(size, split.teamB.size)
            assertEquals(
                emptySet<Long>(),
                split.teamA.toSet().intersect(split.teamB.toSet())
            )
            assertEquals(players(size * 2).toSet(), (split.teamA + split.teamB).toSet())
        }
    }

    @Test
    fun `two duos stay together`() {
        val result = TeamGenerator.generate(players(10), 5, listOf(1L to 2L, 3L to 4L))
        val split = (result as TeamGenerator.Result.Success).split
        assertTrue(sameTeam(split, 1L, 2L))
        assertTrue(sameTeam(split, 3L, 4L))
    }

    @Test
    fun `impossible duo combination is detected`() {
        // 3 tegen 3 met 3 duo's kan niet: elke team is oneven (3) en duo's zijn even (2).
        val result = TeamGenerator.generate(players(6), 3, listOf(1L to 2L, 3L to 4L, 5L to 6L))
        assertTrue(result is TeamGenerator.Result.Failure)
    }

    @Test
    fun `duo with unselected player fails`() {
        val result = TeamGenerator.generate(players(6), 3, listOf(1L to 99L))
        assertTrue(result is TeamGenerator.Result.Failure)
    }

    @Test
    fun `player in two duos fails`() {
        val result = TeamGenerator.generate(players(10), 5, listOf(1L to 2L, 2L to 3L))
        assertTrue(result is TeamGenerator.Result.Failure)
    }

    @Test
    fun `wrong number of players fails`() {
        val result = TeamGenerator.generate(players(5), 3, emptyList())
        assertTrue(result is TeamGenerator.Result.Failure)
    }

    private fun sameTeam(
        split: nl.tartaruzz.teamcreatorr.domain.TeamSplit,
        a: Long,
        b: Long
    ): Boolean = (a in split.teamA && b in split.teamA) || (a in split.teamB && b in split.teamB)
}
