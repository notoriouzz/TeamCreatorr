package nl.tartaruzz.teamcreatorr.domain

/**
 * Builds a shareable, WhatsApp-friendly text representation of a team split.
 * Pure Kotlin so it can be unit-tested.
 */
object ShareTextBuilder {

    fun teamIndeling(teamA: List<PlayerRef>, teamB: List<PlayerRef>): String {
        val sb = StringBuilder()
        sb.appendLine("🔥 DE TEAMINDELING IS BEKEND! 🔥")
        sb.appendLine()
        sb.appendLine("🔵 TEAM A")
        sb.appendLine()
        teamA.forEachIndexed { index, player ->
            sb.appendLine("${index + 1}. ${playerLine(player)}")
        }
        sb.appendLine()
        sb.appendLine("🔴 TEAM B")
        sb.appendLine()
        teamB.forEachIndexed { index, player ->
            sb.appendLine("${index + 1}. ${playerLine(player)}")
        }
        return sb.toString().trimEnd()
    }

    private fun playerLine(player: PlayerRef): String =
        if (player.nickname.isNullOrBlank()) player.name
        else "${player.name} — \"${player.nickname}\""
}
