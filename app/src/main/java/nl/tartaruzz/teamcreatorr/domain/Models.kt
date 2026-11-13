package nl.tartaruzz.teamcreatorr.domain

/** Team assignment for a single match. */
data class TeamSplit(
    val teamA: List<Long>,
    val teamB: List<Long>
)

/** Point settings used for ranking calculation. */
data class PointSettings(
    val attendancePoints: Int,
    val winPoints: Int
)

/** Lightweight player input for the ranking calculator. */
data class PlayerInput(
    val id: Long,
    val name: String,
    val active: Boolean
)

/** A single participation row (player + team + match result). */
data class Participation(
    val playerId: Long,
    val team: String,
    val scoreA: Int?,
    val scoreB: Int?
)

/** Computed statistics for one player, including ranking position. */
data class PlayerStats(
    val playerId: Long,
    val name: String,
    val active: Boolean,
    val played: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val attendancePoints: Int,
    val winPoints: Int,
    val totalPoints: Int,
    val rank: Int
) {
    /** Number of matches that actually have a result. */
    val decided: Int get() = wins + losses + draws

    /** Win percentage over decided matches (0 when no decided matches). */
    val winPercentage: Int
        get() = if (decided == 0) 0 else Math.round(wins * 100.0 / decided).toInt()
}

/** References a player by stable id + current name. */
data class PlayerRef(
    val id: Long,
    val name: String
)

/** Full detail of a match, including both teams with player names. */
data class MatchDetail(
    val id: Long,
    val date: Long,
    val note: String?,
    val teamSize: Int,
    val scoreA: Int?,
    val scoreB: Int?,
    val teamA: List<PlayerRef>,
    val teamB: List<PlayerRef>
) {
    val hasResult: Boolean get() = scoreA != null && scoreB != null

    val winnerTeam: String?
        get() = when {
            scoreA == null || scoreB == null -> null
            scoreA > scoreB -> "A"
            scoreB > scoreA -> "B"
            else -> null
        }

    val isDraw: Boolean get() = scoreA != null && scoreB != null && scoreA == scoreB
}

/** A duo (two players that must stay together). */
data class DuoWithNames(
    val id: Long,
    val playerA: PlayerRef,
    val playerB: PlayerRef
)

/** One entry in a player's personal match history. */
data class PlayerHistoryItem(
    val matchId: Long,
    val date: Long,
    val team: String,
    val scoreA: Int?,
    val scoreB: Int?,
    val note: String?
)

/** Full player profile data. */
data class PlayerProfileUiData(
    val playerId: Long,
    val name: String,
    val active: Boolean,
    val stats: PlayerStats?,
    val history: List<PlayerHistoryItem>
)
