package de.moosfett.notificationbundler.data.db

import androidx.room.*
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE delivered = 0 AND skipped = 0 AND critical = 0 ORDER BY postTime ASC")
    suspend fun pending(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE date(postTime/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun countToday(): Flow<Int>

    @Update
    suspend fun update(e: NotificationEntity)

    @Query("UPDATE notifications SET delivered=1 WHERE id IN (:ids)")
    suspend fun markDelivered(ids: List<Long>)

    @Query("DELETE FROM notifications WHERE postTime < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("SELECT DISTINCT packageName FROM notifications ORDER BY packageName")
    suspend fun seenPackages(): List<String>
}
