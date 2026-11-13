package nl.tartaruzz.teamcreatorr.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.PlayerInput
import nl.tartaruzz.teamcreatorr.domain.PlayerProfileUiData
import nl.tartaruzz.teamcreatorr.domain.PlayerRef
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.domain.PointSettings
import nl.tartaruzz.teamcreatorr.domain.RankingCalculator

class Repository(private val db: TeamCreatorrDatabase) {

    // ---- Players ----------------------------------------------------------

    fun observePlayers(): Flow<List<PlayerEntity>> = db.playerDao().observeAll()

    fun observeActivePlayers(): Flow<List<PlayerEntity>> = db.playerDao().observeActive()

    fun observePlayer(id: Long): Flow<PlayerEntity?> = db.playerDao().observeById(id)

    suspend fun addPlayer(name: String): Long =
        db.playerDao().insert(PlayerEntity(name = name.trim()))

    suspend fun addPlayers(names: List<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        clean.forEach { db.playerDao().insert(PlayerEntity(name = it)) }
    }

    suspend fun renamePlayer(id: Long, name: String) {
        db.playerDao().updateName(id, name.trim())
    }

    suspend fun setPlayerActive(id: Long, active: Boolean) {
        db.playerDao().updateActive(id, active)
    }

    /**
     * Hard-deletes a player only when they have no match history.
     * Otherwise the player is deactivated so history stays intact.
     * Returns true when the player was actually deleted.
     */
    suspend fun deletePlayer(id: Long): Boolean {
        val hasHistory = db.matchPlayerDao().countForPlayer(id) > 0
        return if (hasHistory) {
            db.playerDao().updateActive(id, false)
            false
        } else {
            db.playerDao().deleteById(id)
            true
        }
    }

    // ---- Duos ------------------------------------------------------------

    fun observeDuos(): Flow<List<DuoWithNames>> =
        combine(db.duoDao().observeAll(), db.playerDao().observeAll()) { duos, players ->
            val byId = players.associateBy { it.id }
            duos.mapNotNull { duo ->
                val a = byId[duo.playerAId] ?: return@mapNotNull null
                val b = byId[duo.playerBId] ?: return@mapNotNull null
                DuoWithNames(
                    id = duo.id,
                    playerA = PlayerRef(a.id, a.name),
                    playerB = PlayerRef(b.id, b.name)
                )
            }
        }

    sealed interface DuoResult {
        data object Success : DuoResult
        data class Error(val message: String) : DuoResult
    }

    suspend fun addDuo(playerAId: Long, playerBId: Long): DuoResult {
        if (playerAId == playerBId) return DuoResult.Error("Kies twee verschillende spelers.")
        val duos = db.duoDao().observeAll().first()
        val used = mutableSetOf<Long>()
        for (d in duos) {
            used.add(d.playerAId)
            used.add(d.playerBId)
        }
        if (playerAId in used || playerBId in used) {
            return DuoResult.Error("Eén van deze spelers zit al in een ander duo.")
        }
        val a = db.playerDao().observeById(playerAId).first()
        val b = db.playerDao().observeById(playerBId).first()
        if (a == null || b == null) return DuoResult.Error("Speler niet gevonden.")
        if (!a.active || !b.active) return DuoResult.Error("Beide spelers moeten actief zijn.")
        db.duoDao().insert(DuoEntity(playerAId = playerAId, playerBId = playerBId))
        return DuoResult.Success
    }

    suspend fun removeDuo(id: Long) {
        db.duoDao().deleteById(id)
    }

    // ---- Settings ---------------------------------------------------------

    fun observeSettings(): Flow<PointSettings> =
        db.settingsDao().observe().map { settings -> settings ?: SettingsEntity() }
            .map { PointSettings(it.attendancePoints, it.winPoints) }

    suspend fun getSettings(): PointSettings {
        val s = db.settingsDao().get() ?: SettingsEntity().also { db.settingsDao().insert(it) }
        return PointSettings(s.attendancePoints, s.winPoints)
    }

    suspend fun updateSettings(attendancePoints: Int, winPoints: Int) {
        val current = db.settingsDao().get()
        if (current == null) {
            db.settingsDao().insert(
                SettingsEntity(id = 1, attendancePoints = attendancePoints, winPoints = winPoints)
            )
        } else {
            db.settingsDao().update(
                current.copy(attendancePoints = attendancePoints, winPoints = winPoints)
            )
        }
    }

    // ---- Matches ----------------------------------------------------------

    fun observeMatches(): Flow<List<MatchEntity>> = db.matchDao().observeAll()

    fun observeMatchDetails(): Flow<List<MatchDetail>> =
        combine(
            db.matchDao().observeAll(),
            db.matchPlayerDao().observeAllRows(),
            db.playerDao().observeAll()
        ) { matches, rows, players ->
            val names = players.associate { it.id to it.name }
            matches.map { m -> toDetail(m, rows, names) }
        }

    fun observeMatchDetail(id: Long): Flow<MatchDetail?> =
        observeMatchDetails().map { list -> list.firstOrNull { it.id == id } }

    private fun toDetail(
        match: MatchEntity,
        rows: List<MatchPlayerEntity>,
        names: Map<Long, String>
    ): MatchDetail {
        val teamA = rows.filter { it.matchId == match.id && it.team == "A" }
            .map { PlayerRef(it.playerId, names[it.playerId] ?: "Speler") }
            .sortedBy { it.name.lowercase() }
        val teamB = rows.filter { it.matchId == match.id && it.team == "B" }
            .map { PlayerRef(it.playerId, names[it.playerId] ?: "Speler") }
            .sortedBy { it.name.lowercase() }
        return MatchDetail(
            id = match.id,
            date = match.date,
            note = match.note,
            teamSize = match.teamSize,
            scoreA = match.scoreA,
            scoreB = match.scoreB,
            teamA = teamA,
            teamB = teamB
        )
    }

    suspend fun createMatch(
        date: Long,
        note: String?,
        teamSize: Int,
        teamA: List<Long>,
        teamB: List<Long>
    ): Long = db.withTransaction {
        val matchId = db.matchDao().insert(
            MatchEntity(date = date, note = note?.trim()?.ifEmpty { null }, teamSize = teamSize)
        )
        val entries = teamA.map { MatchPlayerEntity(matchId, it, "A") } +
            teamB.map { MatchPlayerEntity(matchId, it, "B") }
        db.matchPlayerDao().insertAll(entries)
        matchId
    }

    suspend fun updateMatchResult(id: Long, scoreA: Int, scoreB: Int) {
        db.matchDao().updateScore(id, scoreA, scoreB)
    }

    suspend fun updateMatchDetails(id: Long, date: Long, note: String?) {
        db.matchDao().updateDetails(id, date, note?.trim()?.ifEmpty { null })
    }

    suspend fun deleteMatch(id: Long) {
        db.matchDao().deleteById(id)
    }

    // ---- Ranking ----------------------------------------------------------

    fun observeRanking(): Flow<List<PlayerStats>> =
        combine(
            db.playerDao().observeAll(),
            db.matchPlayerDao().observeParticipations(),
            db.settingsDao().observe()
        ) { players, participations, settings ->
            val s = settings ?: SettingsEntity()
            RankingCalculator.compute(
                players.map { PlayerInput(it.id, it.name, it.active) },
                participations,
                PointSettings(s.attendancePoints, s.winPoints)
            )
        }

    // ---- Player profile ---------------------------------------------------

    fun observePlayerProfile(playerId: Long): Flow<PlayerProfileUiData?> =
        combine(
            db.playerDao().observeById(playerId),
            observeRanking(),
            db.matchPlayerDao().observeHistoryForPlayer(playerId)
        ) { player, ranking, history ->
            player?.let {
                PlayerProfileUiData(
                    playerId = it.id,
                    name = it.name,
                    active = it.active,
                    stats = ranking.firstOrNull { s -> s.playerId == it.id },
                    history = history
                )
            }
        }
}

