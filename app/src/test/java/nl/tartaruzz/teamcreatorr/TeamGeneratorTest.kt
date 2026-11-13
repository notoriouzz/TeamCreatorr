package nl.tartaruzz.teamcreatorr

import kotlin.math.abs
import nl.tartaruzz.teamcreatorr.domain.TeamGenerator
import nl.tartaruzz.teamcreatorr.domain.TeamSplit
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
            assertEquals(emptySet<Long>(), split.teamA.toSet().intersect(split.teamB.toSet()))
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

    @Test
    fun `balanced generation produces equal teams and keeps duos together`() {
        val ratings = mapOf(1L to 90, 2L to 85, 3L to 80, 4L to 30, 5L to 25, 6L to 20)
        val result = TeamGenerator.generateBalanced(players(6), 3, listOf(1L to 2L), ratings)
        assertTrue(result is TeamGenerator.Result.Success)
        val split = (result as TeamGenerator.Result.Success).split
        assertEquals(3, split.teamA.size)
        assertEquals(3, split.teamB.size)
        assertTrue(sameTeam(split, 1L, 2L))
    }

    @Test
    fun `balanced generation reduces rating difference`() {
        val ratings = mapOf(1L to 90, 2L to 85, 3L to 80, 4L to 30, 5L to 25, 6L to 20)
        val result = TeamGenerator.generateBalanced(players(6), 3, emptyList(), ratings)
        val split = (result as TeamGenerator.Result.Success).split
        val sumA = split.teamA.sumOf { ratings[it] ?: 50 }
        val sumB = split.teamB.sumOf { ratings[it] ?: 50 }
        assertTrue("Teams not balanced enough (diff=${abs(sumA - sumB)})", abs(sumA - sumB) <= 40)
    }

    private fun sameTeam(split: TeamSplit, a: Long, b: Long): Boolean =
        (a in split.teamA && b in split.teamA) || (a in split.teamB && b in split.teamB)
}
