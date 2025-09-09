package de.moosfett.notificationbundler.data.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.moosfett.notificationbundler.data.db.AppDatabase
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    private val db by lazy { AppDatabase.getInstance(appContext) }

    suspend fun insert(e: NotificationEntity) = db.notifications().insert(e)

    suspend fun pending(): List<NotificationEntity> = db.notifications().pending()

    suspend fun markDelivered(ids: List<Long>) = db.notifications().markDelivered(ids)

    suspend fun deleteOlderThan(threshold: Long) = db.notifications().deleteOlderThan(threshold)

    suspend fun seenPackages(): List<String> = db.notifications().seenPackages()

    fun countToday(): Flow<Int> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = start + 24L * 60L * 60L * 1000L - 1
        return db.notifications().countToday(start, end)
    }
}
