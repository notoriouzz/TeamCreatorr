package nl.tartaruzz.teamcreatorr.domain

import kotlin.random.Random

/**
 * Generates two equally sized teams from a set of players while respecting
 * "keep together" duos. Pure Kotlin so it can be unit-tested on the JVM.
 */
object TeamGenerator {

    sealed interface Result {
        data class Success(val split: TeamSplit) : Result
        data class Failure(val reason: String) : Result
    }

    fun generate(
        players: List<Long>,
        teamSize: Int,
        duos: List<Pair<Long, Long>>,
        random: Random = Random.Default
    ): Result {
        if (teamSize < 3 || teamSize > 11) {
            return Result.Failure("Ongeldige teamgrootte. Kies 3 tot en met 11 spelers per team.")
        }
        val required = teamSize * 2
        if (players.size != required) {
            return Result.Failure("Er zijn precies $required spelers nodig voor $teamSize tegen $teamSize (nu ${players.size}).")
        }
        if (players.distinct().size != players.size) {
            return Result.Failure("Een speler kan niet twee keer geselecteerd worden.")
        }

        val selected = players.toSet()
        val playerInDuo = mutableSetOf<Long>()
        for ((a, b) in duos) {
            if (a == b) return Result.Failure("Een duo moet uit twee verschillende spelers bestaan.")
            if (a !in selected || b !in selected) {
                return Result.Failure("Een duo bevat een speler die niet is geselecteerd.")
            }
            if (!playerInDuo.add(a)) return Result.Failure("Een speler kan in maximaal één duo zitten.")
            if (!playerInDuo.add(b)) return Result.Failure("Een speler kan in maximaal één duo zitten.")
        }

        // Groups: each duo is a fixed group of two; all others are single groups.
        val groups = mutableListOf<List<Long>>()
        for ((a, b) in duos) groups.add(listOf(a, b))
        for (p in players) if (p !in playerInDuo) groups.add(listOf(p))

        if (groups.any { it.size > teamSize }) {
            return Result.Failure("Een gekoppeld duo past niet in een team van $teamSize spelers.")
        }

        // Precise impossibility check: with k duos of size 2 we can place at most
        // floor(teamSize / 2) duos in each team, so 2 * floor(teamSize / 2) in total.
        val maxDuos = 2 * (teamSize / 2)
        if (duos.size > maxDuos) {
            return Result.Failure(
                "Deze duo-combinatie is niet mogelijk: je hebt ${duos.size} duo's ingesteld, " +
                    "maar bij $teamSize tegen $teamSize passen maximaal $maxDuos duo's over twee even teams. " +
                    "Ontkoppel minimaal ${duos.size - maxDuos} duo('s)."
            )
        }

        val shuffled = groups.shuffled(random)
        val teamA = mutableListOf<Long>()
        val teamB = mutableListOf<Long>()
        val found = assign(shuffled, 0, teamA, teamB, teamSize, random)
        if (!found) {
            return Result.Failure(
                "Geen geldige verdeling gevonden voor deze duo-combinatie. " +
                    "Probeer één of meer duo's los te koppelen."
            )
        }
        return Result.Success(TeamSplit(teamA = teamA.toList(), teamB = teamB.toList()))
    }

    private fun assign(
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
                if (assign(groups, index + 1, teamA, teamB, teamSize, random)) return true
                repeat(added) { target.removeAt(target.lastIndex) }
            }
        }
        return false
    }
}
