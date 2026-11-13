package nl.tartaruzz.teamcreatorr.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val note: String? = null,
    val teamSize: Int,
    val scoreA: Int? = null,
    val scoreB: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "match_players",
    primaryKeys = ["matchId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId"), Index("matchId")]
)
data class MatchPlayerEntity(
    val matchId: Long,
    val playerId: Long,
    val team: String
)

@Entity(
    tableName = "duos",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerAId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerBId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerAId"), Index("playerBId")]
)
data class DuoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerAId: Long,
    val playerBId: Long
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val attendancePoints: Int = 1,
    val winPoints: Int = 2
)
