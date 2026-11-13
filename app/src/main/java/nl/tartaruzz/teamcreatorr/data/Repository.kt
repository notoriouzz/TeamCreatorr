package nl.tartaruzz.teamcreatorr.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.MatchStatus
import nl.tartaruzz.teamcreatorr.domain.ManualAdjustment
import nl.tartaruzz.teamcreatorr.domain.PlayerHistoryItem
import nl.tartaruzz.teamcreatorr.domain.PlayerInput
import nl.tartaruzz.teamcreatorr.domain.PlayerProfileUiData
import nl.tartaruzz.teamcreatorr.domain.PlayerRef
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.domain.PointHistoryEntry
import nl.tartaruzz.teamcreatorr.domain.PointSettings
import nl.tartaruzz.teamcreatorr.domain.RankingCalculator
import nl.tartaruzz.teamcreatorr.domain.RatingCalculator

class Repository(private val db: TeamCreatorrDatabase) {

    // ---- Players ----------------------------------------------------------

    fun observePlayers(): Flow<List<PlayerEntity>> = db.playerDao().observeAll()

    fun observeActivePlayers(): Flow<List<PlayerEntity>> = db.playerDao().observeActive()

    fun observePlayer(id: Long): Flow<PlayerEntity?> = db.playerDao().observeById(id)

    suspend fun addPlayer(name: String, nickname: String?): Long =
        db.playerDao().insert(
            PlayerEntity(name = name.trim(), nickname = nickname?.trim()?.ifEmpty { null })
        )

    suspend fun addPlayers(names: List<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        clean.forEach { db.playerDao().insert(PlayerEntity(name = it)) }
    }

    suspend fun updatePlayer(id: Long, name: String, nickname: String?) {
        db.playerDao().updateNameAndNickname(id, name.trim(), nickname?.trim()?.ifEmpty { null })
    }

    suspend fun updatePlayerPhoto(id: Long, photoFileName: String?) {
        db.playerDao().updatePhotoFileName(id, photoFileName)
    }

    suspend fun setPlayerActive(id: Long, active: Boolean) {
        db.playerDao().updateActive(id, active)
    }

    /** Hard-deletes only when there is no history; otherwise deactivates. */
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

    // ---- Duos (global, managed in Settings) --------------------------------

    fun observeDuos(): Flow<List<DuoWithNames>> =
        combine(db.duoDao().observeAll(), db.playerDao().observeAll()) { duos, players ->
            val byId = players.associateBy { it.id }
            duos.mapNotNull { duo ->
                val a = byId[duo.playerAId] ?: return@mapNotNull null
                val b = byId[duo.playerBId] ?: return@mapNotNull null
                DuoWithNames(
                    id = duo.id,
                    playerA = PlayerRef(a.id, a.name, a.nickname),
                    playerB = PlayerRef(b.id, b.name, b.nickname)
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
            .map { PointSettings(it.attendancePoints, it.winPoints, it.drawPoints) }

    suspend fun getSettings(): PointSettings {
        val s = db.settingsDao().get() ?: SettingsEntity().also { db.settingsDao().insert(it) }
        return PointSettings(s.attendancePoints, s.winPoints, s.drawPoints)
    }

    suspend fun updateSettings(attendancePoints: Int, winPoints: Int, drawPoints: Int) {
        val current = db.settingsDao().get()
        if (current == null) {
            db.settingsDao().insert(
                SettingsEntity(
                    id = 1,
                    attendancePoints = attendancePoints,
                    winPoints = winPoints,
                    drawPoints = drawPoints
                )
            )
        } else {
            db.settingsDao().update(
                current.copy(
                    attendancePoints = attendancePoints,
                    winPoints = winPoints,
                    drawPoints = drawPoints
                )
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
            val byId = players.associate { it.id to it }
            matches.map { m -> toDetail(m, rows, byId) }
        }

    fun observeMatchDetail(id: Long): Flow<MatchDetail?> =
        observeMatchDetails().map { list -> list.firstOrNull { it.id == id } }

    fun observeMatchDuos(id: Long): Flow<List<Pair<Long, Long>>> =
        db.matchDuoDao().observeForMatch(id).map { rows ->
            rows.map { it.playerAId to it.playerBId }
        }

    private fun toDetail(
        match: MatchEntity,
        rows: List<MatchPlayerEntity>,
        players: Map<Long, PlayerEntity>
    ): MatchDetail {
        fun ref(playerId: Long, team: String): PlayerRef =
            players[playerId]?.let { PlayerRef(it.id, it.name, it.nickname) }
                ?: PlayerRef(playerId, "Speler")

        val teamA = rows.filter { it.matchId == match.id && it.team == "A" }
            .map { ref(it.playerId, "A") }
            .sortedBy { it.name.lowercase() }
        val teamB = rows.filter { it.matchId == match.id && it.team == "B" }
            .map { ref(it.playerId, "B") }
            .sortedBy { it.name.lowercase() }
        return MatchDetail(
            id = match.id,
            date = match.date,
            teamSize = match.teamSize,
            scoreA = match.scoreA,
            scoreB = match.scoreB,
            teamA = teamA,
            teamB = teamB,
            status = MatchStatus.fromCode(match.status)
        )
    }

    suspend fun createMatch(
        date: Long,
        teamSize: Int,
        teamA: List<Long>,
        teamB: List<Long>,
        duos: List<Pair<Long, Long>>
    ): Long = db.withTransaction {
        val matchId = db.matchDao().insert(
            MatchEntity(date = date, teamSize = teamSize, status = 0)
        )
        val entries = teamA.map { MatchPlayerEntity(matchId, it, "A") } +
            teamB.map { MatchPlayerEntity(matchId, it, "B") }
        db.matchPlayerDao().insertAll(entries)
        if (duos.isNotEmpty()) {
            db.matchDuoDao().insertAll(duos.map { MatchDuoEntity(matchId, it.first, it.second) })
        }
        matchId
    }

    suspend fun markMatchPlayed(id: Long) {
        db.matchDao().markPlayed(id)
    }

    suspend fun updateMatchResult(id: Long, scoreA: Int, scoreB: Int) {
        db.matchDao().updateScore(id, scoreA, scoreB)
    }

    suspend fun updateMatchDetails(id: Long, date: Long) {
        db.matchDao().updateDetails(id, date)
    }

    suspend fun deleteMatch(id: Long) {
        db.matchDao().deleteById(id)
    }

    // ---- Ranking & ratings -------------------------------------------------

    fun observeRanking(): Flow<List<PlayerStats>> =
        combine(
            db.playerDao().observeAll(),
            db.matchPlayerDao().observeParticipations(),
            db.pointAdjustmentDao().observeAllForRanking(),
            db.settingsDao().observe()
        ) { players, participations, pointRows, settings ->
            val s = settings ?: SettingsEntity()
            val manualMap = pointRows.groupBy { it.playerId }
                .mapValues { (_, v) -> v.sumOf { it.points } }
            RankingCalculator.compute(
                players.map { PlayerInput(it.id, it.name, it.nickname, it.active, it.photoFileName) },
                participations,
                manualMap,
                PointSettings(s.attendancePoints, s.winPoints, s.drawPoints)
            )
        }

    fun observeRatings(): Flow<Map<Long, Int>> =
        combine(observeRanking(), observeSettings()) { ranking, settings ->
            ranking.associate {
                it.playerId to RatingCalculator.rating(it.played, it.autoPoints, settings)
            }
        }

    // ---- Point adjustments -------------------------------------------------

    fun observeAdjustments(playerId: Long): Flow<List<ManualAdjustment>> =
        db.pointAdjustmentDao().observeForPlayer(playerId)

    suspend fun addAdjustment(playerId: Long, points: Int, description: String): Long =
        db.pointAdjustmentDao().insert(
            PointAdjustmentEntity(playerId = playerId, points = points, description = description.trim())
        )

    suspend fun updateAdjustment(id: Long, points: Int, description: String) {
        val existing = db.pointAdjustmentDao().getById(id) ?: return
        db.pointAdjustmentDao().update(existing.copy(points = points, description = description.trim()))
    }

    suspend fun deleteAdjustment(id: Long) {
        db.pointAdjustmentDao().deleteById(id)
    }

    // ---- Player profile ----------------------------------------------------

    private data class ProfileParts(
        val player: PlayerEntity?,
        val ranking: List<PlayerStats>,
        val ratings: Map<Long, Int>,
        val history: List<PlayerHistoryItem>,
        val adjustments: List<ManualAdjustment>
    )

    fun observePlayerProfile(playerId: Long): Flow<PlayerProfileUiData?> =
        combine(
            db.playerDao().observeById(playerId),
            observeRanking(),
            observeRatings(),
            db.matchPlayerDao().observeHistoryForPlayer(playerId),
            db.pointAdjustmentDao().observeForPlayer(playerId)
        ) { player, ranking, ratings, history, adjustments ->
            ProfileParts(player, ranking, ratings, history, adjustments)
        }.combine(observeSettings()) { parts, settings ->
            parts.player?.let {
                PlayerProfileUiData(
                    playerId = it.id,
                    name = it.name,
                    nickname = it.nickname,
                    active = it.active,
                    photoFileName = it.photoFileName,
                    rating = parts.ratings[it.id] ?: RatingCalculator.NEUTRAL_RATING,
                    stats = parts.ranking.firstOrNull { s -> s.playerId == it.id },
                    history = parts.history,
                    pointHistory = buildPointHistory(parts.history, parts.adjustments, settings),
                    manualAdjustments = parts.adjustments
                )
            }
        }

    private fun buildPointHistory(
        history: List<PlayerHistoryItem>,
        adjustments: List<ManualAdjustment>,
        settings: PointSettings
    ): List<PointHistoryEntry> {
        val entries = mutableListOf<PointHistoryEntry>()
        for (h in history) {
            if (h.scoreA == null || h.scoreB == null) continue
            entries.add(PointHistoryEntry(h.date, "Aanwezigheid", settings.attendancePoints, false))
            val won = (h.team == "A" && h.scoreA > h.scoreB) || (h.team == "B" && h.scoreB > h.scoreA)
            when {
                h.scoreA == h.scoreB -> entries.add(PointHistoryEntry(h.date, "Gelijkspel", settings.drawPoints, false))
                won -> entries.add(PointHistoryEntry(h.date, "Winst", settings.winPoints, false))
                else -> Unit
            }
        }
        for (a in adjustments) {
            entries.add(PointHistoryEntry(a.createdAt, a.description, a.points, true, a.id))
        }
        return entries.sortedByDescending { it.date }
    }

    // ---- Backup & reset ---------------------------------------------------

    suspend fun exportBackupJson(): String {
        val payload = Backup.Payload(
            settings = db.settingsDao().get() ?: SettingsEntity(),
            players = db.playerDao().getAll(),
            matches = db.matchDao().getAll(),
            matchPlayers = db.matchPlayerDao().getAllRows(),
            duos = db.duoDao().getAll(),
            matchDuos = db.matchDuoDao().getAll(),
            adjustments = db.pointAdjustmentDao().getAll()
        )
        return Backup.toJson(payload)
    }

    sealed interface ImportResult {
        data class Success(val players: Int, val matches: Int, val duos: Int) : ImportResult
        data object Invalid : ImportResult
    }

    suspend fun importBackupJson(json: String, replace: Boolean): ImportResult {
        val payload = Backup.parse(json) ?: return ImportResult.Invalid
        db.withTransaction {
            if (replace) {
                db.matchDao().deleteAll()
                db.matchDuoDao().deleteAll()
                db.duoDao().deleteAll()
                db.pointAdjustmentDao().deleteAll()
                db.playerDao().deleteAll()
            }
            val existing = db.playerDao().getAll().associateBy { it.name.trim().lowercase() }
            val playerIdMap = mutableMapOf<Long, Long>()
            for (p in payload.players) {
                val prev = if (replace) null else existing[p.name.trim().lowercase()]
                val newId = if (prev != null) prev.id
                else db.playerDao().insert(PlayerEntity(name = p.name, nickname = p.nickname, active = p.active, createdAt = p.createdAt))
                playerIdMap[p.id] = newId
            }
            val matchIdMap = mutableMapOf<Long, Long>()
            for (m in payload.matches) {
                val newId = db.matchDao().insert(MatchEntity(date = m.date, teamSize = m.teamSize, scoreA = m.scoreA, scoreB = m.scoreB, status = m.status, createdAt = m.createdAt))
                matchIdMap[m.id] = newId
            }
            for (mp in payload.matchPlayers) {
                val pid = playerIdMap[mp.playerId] ?: continue
                val mid = matchIdMap[mp.matchId] ?: continue
                db.matchPlayerDao().insertAll(listOf(MatchPlayerEntity(mid, pid, mp.team)))
            }
            for (d in payload.duos) {
                val a = playerIdMap[d.playerAId] ?: continue
                val b = playerIdMap[d.playerBId] ?: continue
                db.duoDao().insert(DuoEntity(playerAId = a, playerBId = b))
            }
            for (d in payload.matchDuos) {
                val a = playerIdMap[d.playerAId] ?: continue
                val b = playerIdMap[d.playerBId] ?: continue
                val mid = matchIdMap[d.matchId] ?: continue
                db.matchDuoDao().insertAll(listOf(MatchDuoEntity(mid, a, b)))
            }
            for (a in payload.adjustments) {
                val pid = playerIdMap[a.playerId] ?: continue
                db.pointAdjustmentDao().insert(PointAdjustmentEntity(playerId = pid, points = a.points, description = a.description, createdAt = a.createdAt))
            }
            db.settingsDao().deleteAll()
            db.settingsDao().insert(payload.settings.copy(id = 1))
        }
        return ImportResult.Success(payload.players.size, payload.matches.size, payload.duos.size)
    }

    suspend fun resetAll() = db.withTransaction {
        db.matchDao().deleteAll()
        db.matchDuoDao().deleteAll()
        db.duoDao().deleteAll()
        db.pointAdjustmentDao().deleteAll()
        db.playerDao().deleteAll()
        db.settingsDao().deleteAll()
    }

    suspend fun resetMatches() = db.withTransaction {
        db.matchDao().deleteAll()
    }

    suspend fun resetPlayers() = db.withTransaction {
        db.matchDao().deleteAll()
        db.matchDuoDao().deleteAll()
        db.duoDao().deleteAll()
        db.pointAdjustmentDao().deleteAll()
        db.playerDao().deleteAll()
    }

    suspend fun resetRanking() = db.withTransaction {
        db.pointAdjustmentDao().deleteAll()
    }
}
