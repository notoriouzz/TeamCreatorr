package nl.tartaruzz.teamcreatorr.ui.navigation

object Routes {
    const val HOME = "home"
    const val WEDSTRIJD = "wedstrijd"
    const val NEW_MATCH = "new_match"
    const val RANKING = "ranking"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PLAYERS = "players"
    const val PLAYER = "player/{playerId}"
    const val MATCH = "match/{matchId}"

    fun player(id: Long) = "player/$id"
    fun match(id: Long) = "match/$id"
}
