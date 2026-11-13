package nl.tartaruzz.teamcreatorr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerEntity::class,
        MatchEntity::class,
        MatchPlayerEntity::class,
        DuoEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TeamCreatorrDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun matchPlayerDao(): MatchPlayerDao
    abstract fun duoDao(): DuoDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: TeamCreatorrDatabase? = null

        fun get(context: Context): TeamCreatorrDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TeamCreatorrDatabase::class.java,
                    "teamcreatorr.db"
                ).build().also { instance = it }
            }
    }
}
