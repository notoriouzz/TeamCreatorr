package nl.tartaruzz.teamcreatorr.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Generates two equally sized teams from a set of players while respecting
 * duo constraints. Pure Kotlin so it can be unit-tested on the JVM.
 *
 * A duo is either [DuoMode.TOGETHER] (both players must end up in the same
 * team) or [DuoMode.SEPARATE] (both players must end up in different teams).
 *
 * Two strategies are available:
 *  - [generate]: random assignment that satisfies every constraint.
 *  - [generateBalanced]: tries to balance the total team ratings.
 */
object TeamGenerator {

    sealed interface Result {
        data class Success(
            val split: TeamSplit,
            val teamARating: Int = 0,
            val teamBRating: Int = 0
        ) : Result

        data class Failure(val reason: String) : Result
    }

    private sealed interface Prepared {
        data class Groups(val groups: List<Group>) : Prepared
        data class Invalid(val reason: String) : Prepared
    }

    /**
     * A single assignment unit. [Fixed] players must stay together in one team,
     * [Separate] players must be split over the two teams.
     */
    private sealed interface Group {
        data class Fixed(val players: List<Long>) : Group
        data class Separate(val a: Long, val b: Long) : Group
    }

    fun generate(
        players: List<Long>,
        teamSize: Int,
        duos: List<DuoConstraint>,
        random: Random = Random.Default
    ): Result {
        val prepared = prepare(players, teamSize, duos)
        if (prepared is Prepared.Invalid) return Result.Failure(prepared.reason)
        val groups = (prepared as Prepared.Groups).groups

        val teamA = mutableListOf<Long>()
        val teamB = mutableListOf<Long>()
        if (!backtrack(groups.shuffled(random), 0, teamA, teamB, teamSize, random)) {
            return Result.Failure(INFEASIBLE)
        }
        return Result.Success(TeamSplit(teamA = teamA.toList(), teamB = teamB.toList()))
    }

    fun generateBalanced(
        players: List<Long>,
        teamSize: Int,
        duos: List<DuoConstraint>,
        ratings: Map<Long, Int>,
        random: Random = Random.Default
    ): Result {
        val prepared = prepare(players, teamSize, duos)
        if (prepared is Prepared.Invalid) return Result.Failure(prepared.reason)
        val groups = (prepared as Prepared.Groups).groups

        val balanced = assignBalanced(groups, teamSize, ratings, random)
        if (balanced == null) {
            val result = generate(players, teamSize, duos, random)
            return if (result is Result.Success) {
                Result.Success(
                    TeamSplit(result.split.teamA, result.split.teamB),
                    teamRating(result.split.teamA, ratings),
                    teamRating(result.split.teamB, ratings)
                )
            } else result
        }
        return Result.Success(
            balanced,
            teamRating(balanced.teamA, ratings),
            teamRating(balanced.teamB, ratings)
        )
    }

    private fun teamRating(team: List<Long>, ratings: Map<Long, Int>): Int =
        team.sumOf { ratings[it] ?: RatingCalculator.NEUTRAL_RATING }

    /** Best-of-N random valid assignments, keeping the most balanced one. */
    private fun assignBalanced(
        groups: List<Group>,
        teamSize: Int,
        ratings: Map<Long, Int>,
        random: Random
    ): TeamSplit? {
        var best: TeamSplit? = null
        var bestDiff = Int.MAX_VALUE
        repeat(250) {
            val a = mutableListOf<Long>()
            val b = mutableListOf<Long>()
            if (backtrack(groups.shuffled(random), 0, a, b, teamSize, random)) {
                val diff = abs(teamRating(a, ratings) - teamRating(b, ratings))
                if (diff < bestDiff) {
                    bestDiff = diff
                    best = TeamSplit(a.toList(), b.toList())
                }
            }
        }
        return best
    }

    private fun prepare(
        players: List<Long>,
        teamSize: Int,
        duos: List<DuoConstraint>
    ): Prepared {
        if (teamSize < 3 || teamSize > 11) {
            return Prepared.Invalid("Ongeldige teamgrootte. Kies 3 tot en met 11 spelers per team.")
        }
        val required = teamSize * 2
        if (players.size != required) {
            return Prepared.Invalid("Er zijn precies $required spelers nodig voor $teamSize tegen $teamSize (nu ${players.size}).")
        }
        if (players.distinct().size != players.size) {
            return Prepared.Invalid("Een speler kan niet twee keer geselecteerd worden.")
        }

        val selected = players.toSet()
        val playerInDuo = mutableSetOf<Long>()
        val together = mutableListOf<List<Long>>()
        val separate = mutableListOf<Group.Separate>()

        for (c in duos) {
            if (c.playerAId == c.playerBId) {
                return Prepared.Invalid("Een duo moet uit twee verschillende spelers bestaan.")
            }
            if (c.playerAId !in selected || c.playerBId !in selected) {
                return Prepared.Invalid("Een duo bevat een speler die niet is geselecteerd.")
            }
            if (!playerInDuo.add(c.playerAId)) {
                return Prepared.Invalid("Een speler kan in maximaal één duo zitten.")
            }
            if (!playerInDuo.add(c.playerBId)) {
                return Prepared.Invalid("Een speler kan in maximaal één duo zitten.")
            }
            if (c.mode == DuoMode.TOGETHER) {
                together.add(listOf(c.playerAId, c.playerBId))
            } else {
                separate.add(Group.Separate(c.playerAId, c.playerBId))
            }
        }

        // A "together" duo occupies two slots in one team, so at most
        // floor(teamSize/2) of them fit per team.
        val maxTogether = 2 * (teamSize / 2)
        if (together.size > maxTogether) {
            return Prepared.Invalid(
                "Deze samen-duo's zijn niet mogelijk: je hebt ${together.size} samen-duo's ingesteld, " +
                    "maar bij $teamSize tegen $teamSize passen maximaal $maxTogether samen-duo's over twee even teams. " +
                    "Ontkoppel minimaal ${together.size - maxTogether} samen-duo('s)."
            )
        }

        val groups = mutableListOf<Group>()
        together.forEach { groups.add(Group.Fixed(it)) }
        for (p in players) {
            if (p !in playerInDuo) groups.add(Group.Fixed(listOf(p)))
        }
        groups.addAll(separate)

        return Prepared.Groups(groups)
    }

    private fun backtrack(
        groups: List<Group>,
        index: Int,
        teamA: MutableList<Long>,
        teamB: MutableList<Long>,
        teamSize: Int,
        random: Random
    ): Boolean {
        if (index == groups.size) {
            return teamA.size == teamSize && teamB.size == teamSize
        }

        when (val group = groups[index]) {
            is Group.Fixed -> {
                val size = group.players.size
                val tryAFirst = random.nextBoolean()
                val targets = if (tryAFirst) listOf(teamA, teamB) else listOf(teamB, teamA)
                for (target in targets) {
                    if (target.size + size <= teamSize) {
                        target.addAll(group.players)
                        if (backtrack(groups, index + 1, teamA, teamB, teamSize, random)) return true
                        repeat(size) { target.removeAt(target.lastIndex) }
                    }
                }
            }
            is Group.Separate -> {
                val aFirst = random.nextBoolean()
                val orientations =
                    if (aFirst) listOf(group.a to group.b, group.b to group.a)
                    else listOf(group.b to group.a, group.a to group.b)
                for ((toA, toB) in orientations) {
                    if (teamA.size + 1 <= teamSize && teamB.size + 1 <= teamSize) {
                        teamA.add(toA)
                        teamB.add(toB)
                        if (backtrack(groups, index + 1, teamA, teamB, teamSize, random)) return true
                        teamA.removeAt(teamA.lastIndex)
                        teamB.removeAt(teamB.lastIndex)
                    }
                }
            }
        }
        return false
    }

    private const val INFEASIBLE =
        "Geen geldige teamindeling mogelijk met deze duo-instellingen. Pas de duo's aan."
}

