package nl.tartaruzz.teamcreatorr.domain

/**
 * Calculates player statistics and the ranking from raw data.
 * Points are always derived from attendance + results + manual adjustments,
 * so changing point settings automatically recomputes everything.
 * Only matches with a result (FINISHED) contribute points.
 */
object RankingCalculator {

    private enum class Outcome { WIN, LOSS, DRAW, UNDECIDED }

    fun compute(
        players: List<PlayerInput>,
        participations: List<Participation>,
        manualPoints: Map<Long, Int>,
        settings: PointSettings
    ): List<PlayerStats> {
        val stats = players.map { player ->
            val rows = participations.filter { it.playerId == player.id }
            var wins = 0
            var losses = 0
            var draws = 0
            var played = 0
            for (row in rows) {
                when (outcome(row.team, row.scoreA, row.scoreB)) {
                    Outcome.WIN -> { played++; wins++ }
                    Outcome.LOSS -> { played++; losses++ }
                    Outcome.DRAW -> { played++; draws++ }
                    Outcome.UNDECIDED -> Unit
                }
            }
            val attendancePoints = played * settings.attendancePoints
            val winPoints = wins * settings.winPoints
            val drawPoints = draws * settings.drawPoints
            val manual = manualPoints[player.id] ?: 0
            PlayerStats(
                playerId = player.id,
                name = player.name,
                nickname = player.nickname,
                active = player.active,
                played = played,
                wins = wins,
                losses = losses,
                draws = draws,
                attendancePoints = attendancePoints,
                winPoints = winPoints,
                drawPoints = drawPoints,
                manualPoints = manual,
                totalPoints = attendancePoints + winPoints + drawPoints + manual,
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
