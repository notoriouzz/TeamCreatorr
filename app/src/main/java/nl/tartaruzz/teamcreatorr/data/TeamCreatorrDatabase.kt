package nl.tartaruzz.teamcreatorr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerEntity::class,
        MatchEntity::class,
        MatchPlayerEntity::class,
        DuoEntity::class,
        MatchDuoEntity::class,
        PointAdjustmentEntity::class,
        SettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TeamCreatorrDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun matchPlayerDao(): MatchPlayerDao
    abstract fun duoDao(): DuoDao
    abstract fun matchDuoDao(): MatchDuoDao
    abstract fun pointAdjustmentDao(): PointAdjustmentDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: TeamCreatorrDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN nickname TEXT")
                db.execSQL("ALTER TABLE players ADD COLUMN photoFileName TEXT")
                db.execSQL("ALTER TABLE matches ADD COLUMN status INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN drawPoints INTEGER NOT NULL DEFAULT 1")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `match_duos` (" +
                        "`matchId` INTEGER NOT NULL, " +
                        "`playerAId` INTEGER NOT NULL, " +
                        "`playerBId` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`matchId`, `playerAId`, `playerBId`), " +
                        "FOREIGN KEY(`matchId`) REFERENCES `matches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_match_duos_matchId` ON `match_duos` (`matchId`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `point_adjustments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`playerId` INTEGER NOT NULL, " +
                        "`points` INTEGER NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`playerId`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_point_adjustments_playerId` ON `point_adjustments` (`playerId`)"
                )
                db.execSQL("UPDATE matches SET status = 2 WHERE scoreA IS NOT NULL AND scoreB IS NOT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop the (unused) note column from matches. SQLite < 3.35 does not
                // support ALTER TABLE ... DROP COLUMN, so recreate the table.
                db.execSQL("PRAGMA foreign_keys = OFF")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `matches_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`teamSize` INTEGER NOT NULL, " +
                        "`scoreA` INTEGER, " +
                        "`scoreB` INTEGER, " +
                        "`status` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO `matches_new` (`id`,`date`,`teamSize`,`scoreA`,`scoreB`,`status`,`createdAt`) " +
                        "SELECT `id`,`date`,`teamSize`,`scoreA`,`scoreB`,`status`,`createdAt` FROM `matches`"
                )
                db.execSQL("DROP TABLE `matches`")
                db.execSQL("ALTER TABLE `matches_new` RENAME TO `matches`")
                db.execSQL("PRAGMA foreign_keys = ON")
            }
        }

        fun get(context: Context): TeamCreatorrDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TeamCreatorrDatabase::class.java,
                    "teamcreatorr.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
