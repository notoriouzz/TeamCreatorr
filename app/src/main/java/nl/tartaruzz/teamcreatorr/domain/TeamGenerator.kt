package nl.tartaruzz.teamcreatorr.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Generates two equally sized teams from a set of players while respecting
 * "keep together" duos. Pure Kotlin so it can be unit-tested on the JVM.
 *
 * Two strategies are available:
 *  - [generate]: fully random assignment.
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
        data class Groups(val groups: List<List<Long>>) : Prepared
        data class Invalid(val reason: String) : Prepared
    }

    fun generate(
        players: List<Long>,
        teamSize: Int,
        duos: List<Pair<Long, Long>>,
        random: Random = Random.Default
    ): Result {
        val prepared = prepare(players, teamSize, duos)
        if (prepared is Prepared.Invalid) return Result.Failure(prepared.reason)
        val groups = (prepared as Prepared.Groups).groups

        val teamA = mutableListOf<Long>()
        val teamB = mutableListOf<Long>()
        val found = assignRandom(groups.shuffled(random), 0, teamA, teamB, teamSize, random)
        if (!found) {
            return Result.Failure("Geen geldige verdeling gevonden voor deze duo-combinatie.")
        }
        return Result.Success(TeamSplit(teamA = teamA.toList(), teamB = teamB.toList()))
    }

    fun generateBalanced(
        players: List<Long>,
        teamSize: Int,
        duos: List<Pair<Long, Long>>,
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

    /** Best-of-N greedy partition that keeps duos together and balances ratings. */
    private fun assignBalanced(
        groups: List<List<Long>>,
        teamSize: Int,
        ratings: Map<Long, Int>,
        random: Random
    ): TeamSplit? {
        var best: TeamSplit? = null
        var bestDiff = Int.MAX_VALUE
        repeat(100) {
            val shuffled = groups.shuffled(random)
            val a = mutableListOf<Long>()
            val b = mutableListOf<Long>()
            var sumA = 0
            var sumB = 0
            for (g in shuffled) {
                val gRating = g.sumOf { ratings[it] ?: RatingCalculator.NEUTRAL_RATING }
                val canA = a.size + g.size <= teamSize
                val canB = b.size + g.size <= teamSize
                val chooseA = when {
                    !canA -> false
                    !canB -> true
                    sumA < sumB -> true
                    sumB < sumA -> false
                    else -> random.nextBoolean()
                }
                if (chooseA) {
                    a.addAll(g); sumA += gRating
                } else {
                    b.addAll(g); sumB += gRating
                }
            }
            if (a.size == teamSize && b.size == teamSize) {
                val diff = abs(sumA - sumB)
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
        duos: List<Pair<Long, Long>>
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
        for ((a, b) in duos) {
            if (a == b) return Prepared.Invalid("Een duo moet uit twee verschillende spelers bestaan.")
            if (a !in selected || b !in selected) {
                return Prepared.Invalid("Een duo bevat een speler die niet is geselecteerd.")
            }
            if (!playerInDuo.add(a)) return Prepared.Invalid("Een speler kan in maximaal één duo zitten.")
            if (!playerInDuo.add(b)) return Prepared.Invalid("Een speler kan in maximaal één duo zitten.")
        }

        val groups = mutableListOf<List<Long>>()
        for ((a, b) in duos) groups.add(listOf(a, b))
        for (p in players) if (p !in playerInDuo) groups.add(listOf(p))

        if (groups.any { it.size > teamSize }) {
            return Prepared.Invalid("Een gekoppeld duo past niet in een team van $teamSize spelers.")
        }

        val maxDuos = 2 * (teamSize / 2)
        if (duos.size > maxDuos) {
            return Prepared.Invalid(
                "Deze duo-combinatie is niet mogelijk: je hebt ${duos.size} duo's ingesteld, " +
                    "maar bij $teamSize tegen $teamSize passen maximaal $maxDuos duo's over twee even teams. " +
                    "Ontkoppel minimaal ${duos.size - maxDuos} duo('s)."
            )
        }
        return Prepared.Groups(groups)
    }

    private fun assignRandom(
        groups: List<List<Long>>,
        index: Int,
        teamA: MutableList<Long>,
        teamB: MutableList<Long>,
        teamSize: Int,
        random: Random
    ): Boolean {
        if (index == groups.size) {
            return teamA.size == teamSize && teamB.size == teamSize
        }
        val group = groups[index]
        val tryFirstA = random.nextBoolean()
        val targets = if (tryFirstA) listOf(teamA, teamB) else listOf(teamB, teamA)

        for (target in targets) {
            if (target.size + group.size <= teamSize) {
                val added = group.size
                target.addAll(group)
                if (assignRandom(groups, index + 1, teamA, teamB, teamSize, random)) return true
                repeat(added) { target.removeAt(target.lastIndex) }
            }
        }
        return false
    }
}

