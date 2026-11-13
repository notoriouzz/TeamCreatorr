package nl.tartaruzz.teamcreatorr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.tartaruzz.teamcreatorr.domain.ManualAdjustment
import nl.tartaruzz.teamcreatorr.domain.Participation
import nl.tartaruzz.teamcreatorr.domain.PlayerHistoryItem

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY active DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE active = 1 ORDER BY name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun observeById(id: Long): Flow<PlayerEntity?>

    @Insert
    suspend fun insert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE players SET name = :name, nickname = :nickname WHERE id = :id")
    suspend fun updateNameAndNickname(id: Long, name: String, nickname: String?)

    @Query("UPDATE players SET photoFileName = :photoFileName WHERE id = :id")
    suspend fun updatePhotoFileName(id: Long, photoFileName: String?)

    @Query("UPDATE players SET active = :active WHERE id = :id")
    suspend fun updateActive(id: Long, active: Boolean)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    fun observeById(id: Long): Flow<MatchEntity?>

    @Insert
    suspend fun insert(match: MatchEntity): Long

    @Query("UPDATE matches SET scoreA = :scoreA, scoreB = :scoreB, status = 2 WHERE id = :id")
    suspend fun updateScore(id: Long, scoreA: Int, scoreB: Int)

    @Query("UPDATE matches SET status = 1 WHERE id = :id")
    suspend fun markPlayed(id: Long)

    @Query("UPDATE matches SET date = :date, note = :note WHERE id = :id")
    suspend fun updateDetails(id: Long, date: Long, note: String?)

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MatchPlayerDao {
    @Insert
    suspend fun insertAll(entries: List<MatchPlayerEntity>)

    @Query(
        "SELECT mp.playerId AS playerId, mp.team AS team, m.scoreA AS scoreA, m.scoreB AS scoreB " +
            "FROM match_players mp JOIN matches m ON m.id = mp.matchId"
    )
    fun observeParticipations(): Flow<List<Participation>>

    @Query("SELECT COUNT(*) FROM match_players WHERE playerId = :playerId")
    suspend fun countForPlayer(playerId: Long): Int

    @Query(
        "SELECT mp.matchId AS matchId, m.date AS date, mp.team AS team, " +
            "m.scoreA AS scoreA, m.scoreB AS scoreB, m.note AS note, m.status AS status " +
            "FROM match_players mp JOIN matches m ON m.id = mp.matchId " +
            "WHERE mp.playerId = :playerId ORDER BY m.date DESC, m.id DESC"
    )
    fun observeHistoryForPlayer(playerId: Long): Flow<List<PlayerHistoryItem>>

    @Query("SELECT playerId FROM match_players WHERE matchId = :matchId")
    suspend fun playerIdsForMatch(matchId: Long): List<Long>

    @Query("SELECT * FROM match_players")
    fun observeAllRows(): Flow<List<MatchPlayerEntity>>
}

@Dao
interface DuoDao {
    @Query("SELECT * FROM duos ORDER BY id ASC")
    fun observeAll(): Flow<List<DuoEntity>>

    @Insert
    suspend fun insert(duo: DuoEntity): Long

    @Query("DELETE FROM duos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM duos")
    suspend fun deleteAll()
}

@Dao
interface MatchDuoDao {
    @Insert
    suspend fun insertAll(duos: List<MatchDuoEntity>)

    @Query("SELECT playerAId, playerBId FROM match_duos WHERE matchId = :matchId")
    fun observeForMatch(matchId: Long): Flow<List<DuoPair>>

    @Query("DELETE FROM match_duos WHERE matchId = :matchId")
    suspend fun deleteForMatch(matchId: Long)
}

data class DuoPair(
    val playerAId: Long,
    val playerBId: Long
)

@Dao
interface PointAdjustmentDao {
    @Insert
    suspend fun insert(adjustment: PointAdjustmentEntity): Long

    @Update
    suspend fun update(adjustment: PointAdjustmentEntity)

    @Query("DELETE FROM point_adjustments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT id, points, description, createdAt FROM point_adjustments WHERE playerId = :playerId ORDER BY createdAt DESC, id DESC")
    fun observeForPlayer(playerId: Long): Flow<List<ManualAdjustment>>

    @Query("SELECT * FROM point_adjustments WHERE id = :id")
    suspend fun getById(id: Long): PointAdjustmentEntity?

    @Query("SELECT playerId, points FROM point_adjustments")
    fun observeAllForRanking(): Flow<List<PlayerPointsRow>>
}

data class PlayerPointsRow(
    val playerId: Long,
    val points: Int
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Insert
    suspend fun insert(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)
}
