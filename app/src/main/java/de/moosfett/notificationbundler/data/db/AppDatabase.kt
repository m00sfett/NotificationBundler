package de.moosfett.notificationbundler.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class, FilterRuleEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notifications(): NotificationDao
    abstract fun filters(): FiltersDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nb.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
