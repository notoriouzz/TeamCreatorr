package nl.tartaruzz.teamcreatorr.domain

/**
 * Calculates player statistics and the ranking from raw data.
 * Points are always derived from attendance + results, so changing point
 * settings automatically recomputes everything (no stored points can get stale).
 */
object RankingCalculator {

    private enum class Outcome { WIN, LOSS, DRAW, UNDECIDED }

    fun compute(
        players: List<PlayerInput>,
        participations: List<Participation>,
        settings: PointSettings
    ): List<PlayerStats> {
        val stats = players.map { player ->
            val rows = participations.filter { it.playerId == player.id }
            var wins = 0
            var losses = 0
            var draws = 0
            for (row in rows) {
                when (outcome(row.team, row.scoreA, row.scoreB)) {
                    Outcome.WIN -> wins++
                    Outcome.LOSS -> losses++
                    Outcome.DRAW -> draws++
                    Outcome.UNDECIDED -> Unit
                }
            }
            val attendance = rows.size
            val attendancePoints = attendance * settings.attendancePoints
            val winPoints = wins * settings.winPoints
            PlayerStats(
                playerId = player.id,
                name = player.name,
                active = player.active,
                played = attendance,
                wins = wins,
                losses = losses,
                draws = draws,
                attendancePoints = attendancePoints,
                winPoints = winPoints,
                totalPoints = attendancePoints + winPoints,
                rank = 0
            )
        }

        val sorted = stats.sortedWith(
            compareByDescending<PlayerStats> { it.totalPoints }
                .thenByDescending { it.wins }
                .thenBy { it.name.lowercase() }
        )
        return sorted.mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    private fun outcome(team: String, scoreA: Int?, scoreB: Int?): Outcome {
        if (scoreA == null || scoreB == null) return Outcome.UNDECIDED
        return when {
            scoreA > scoreB -> if (team == "A") Outcome.WIN else Outcome.LOSS
            scoreB > scoreA -> if (team == "B") Outcome.WIN else Outcome.LOSS
            else -> Outcome.DRAW
        }
    }
}
