package de.moosfett.notificationbundler.data.repo

import android.content.Context
import de.moosfett.notificationbundler.data.db.AppDatabase
import de.moosfett.notificationbundler.data.entity.NotificationEntity

class NotificationsRepository(private val appContext: Context) {

    private val db by lazy { AppDatabase.getInstance(appContext) }

    suspend fun insert(e: NotificationEntity) = db.notifications().insert(e)

    suspend fun pending(): List<NotificationEntity> = db.notifications().pending()

    suspend fun markDelivered(ids: List<Long>) = db.notifications().markDelivered(ids)

    suspend fun deleteOlderThan(threshold: Long) = db.notifications().deleteOlderThan(threshold)

    suspend fun seenPackages(): List<String> = db.notifications().seenPackages()
}
