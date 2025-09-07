package de.moosfett.notificationbundler.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class, FilterRuleEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notifications(): NotificationDao
    abstract fun filters(): FiltersDao

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

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nb.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
