package nl.tartaruzz.teamcreatorr

import nl.tartaruzz.teamcreatorr.domain.Participation
import nl.tartaruzz.teamcreatorr.domain.PlayerInput
import nl.tartaruzz.teamcreatorr.domain.PointSettings
import nl.tartaruzz.teamcreatorr.domain.RankingCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class RankingCalculatorTest {

    private fun players(vararg names: String): List<PlayerInput> =
        names.mapIndexed { i, n -> PlayerInput((i + 1).toLong(), n, true) }

    private fun part(playerId: Long, team: String, scoreA: Int?, scoreB: Int?) =
        Participation(playerId, team, scoreA, scoreB)

    private val settings = PointSettings(attendancePoints = 1, winPoints = 2)

    @Test
    fun `attendance points are awarded`() {
        val stats = RankingCalculator.compute(
            players("A", "B"),
            listOf(part(1, "A", null, null)),
            settings
        )
        val a = stats.first { it.playerId == 1L }
        assertEquals(1, a.played)
        assertEquals(1, a.attendancePoints)
        assertEquals(0, a.winPoints)
        assertEquals(1, a.totalPoints)
    }

    @Test
    fun `win points are awarded to winners only`() {
        val stats = RankingCalculator.compute(
            players("A", "B"),
            listOf(part(1, "A", 7, 6), part(2, "B", 7, 6)),
            settings
        )
        val a = stats.first { it.playerId == 1L }
        assertEquals(1, a.wins)
        assertEquals(2, a.winPoints)
        assertEquals(3, a.totalPoints)

        val b = stats.first { it.playerId == 2L }
        assertEquals(1, b.losses)
        assertEquals(0, b.winPoints)
        assertEquals(1, b.totalPoints)
    }

    @Test
    fun `draw gives only attendance points`() {
        val stats = RankingCalculator.compute(
            players("A", "B"),
            listOf(part(1, "A", 3, 3), part(2, "B", 3, 3)),
            settings
        )
        val a = stats.first { it.playerId == 1L }
        assertEquals(1, a.draws)
        assertEquals(0, a.winPoints)
        assertEquals(1, a.totalPoints)
    }

    @Test
    fun `historical points are recalculated with new settings`() {
        val participations = listOf(part(1, "A", 7, 6), part(2, "B", 7, 6))
        val old = RankingCalculator.compute(players("A", "B"), participations, PointSettings(1, 2))
        val new = RankingCalculator.compute(players("A", "B"), participations, PointSettings(2, 3))

        assertEquals(3, old.first { it.playerId == 1L }.totalPoints)
        assertEquals(5, new.first { it.playerId == 1L }.totalPoints)
    }

    @Test
    fun `changing a result does not double points`() {
        val win = RankingCalculator.compute(players("A"), listOf(part(1, "A", 7, 6)), settings).first()
        val loss = RankingCalculator.compute(players("A"), listOf(part(1, "A", 6, 7)), settings).first()
        assertEquals(3, win.totalPoints)
        assertEquals(1, loss.totalPoints)
        assertEquals(0, loss.winPoints)
    }

    @Test
    fun `deleting a match removes all its points`() {
        val none = RankingCalculator.compute(players("A"), emptyList(), settings).first()
        assertEquals(0, none.totalPoints)
        assertEquals(0, none.attendancePoints)
        assertEquals(0, none.winPoints)
        assertEquals(0, none.played)
    }

    @Test
    fun `ranking is sorted by points then wins`() {
        val inputs = listOf(
            PlayerInput(1, "Laag", true),
            PlayerInput(2, "Hoog", true),
            PlayerInput(3, "Midden", true)
        )
        val participations = listOf(
            part(2, "A", 5, 0),  // win -> 3 pt
            part(3, "A", 5, 0),  // win -> 3 pt but fewer? equal wins
            part(1, "B", 5, 0)   // loss -> 1 pt
        )
        val ranking = RankingCalculator.compute(inputs, participations, settings)
        assertEquals("Hoog", ranking[0].name)
        assertEquals("Midden", ranking[1].name)
        assertEquals("Laag", ranking[2].name)
        assertEquals(1, ranking[0].rank)
        assertEquals(3, ranking[2].rank)
    }
}
