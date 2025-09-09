package de.moosfett.notificationbundler.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import de.moosfett.notificationbundler.data.entity.DeliveryLogEntity
import de.moosfett.notificationbundler.data.entity.FilterEvaluationEntity

@Database(
    entities = [
        NotificationEntity::class,
        FilterRuleEntity::class,
        DeliveryLogEntity::class,
        FilterEvaluationEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notifications(): NotificationDao
    abstract fun filters(): FiltersDao
    abstract fun deliveryLog(): DeliveryLogDao
    abstract fun filterEvaluations(): FilterEvaluationsDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_postTime ON notifications(postTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_packageName ON notifications(packageName)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS delivery_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, deliveredCount INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_delivery_log_timestamp ON delivery_log(timestamp)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE filter_rules ADD COLUMN patternType TEXT NOT NULL DEFAULT 'EXACT'"
                )
                db.execSQL(
                    "ALTER TABLE filter_rules ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS filter_evaluations (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, notificationKey TEXT, packageName TEXT NOT NULL, postTime INTEGER NOT NULL, ruleId INTEGER, decision TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_filter_evaluations_postTime ON filter_evaluations(postTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_filter_evaluations_packageName ON filter_evaluations(packageName)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nb.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
