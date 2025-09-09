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

    @Query("SELECT COUNT(*) FROM notifications WHERE postTime BETWEEN :start AND :end")
    fun countToday(start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE delivered = 0 AND skipped = 0 AND critical = 0")
    fun pendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE critical = 1 AND delivered = 0")
    fun criticalCount(): Flow<Int>

    @Update
    suspend fun update(e: NotificationEntity)

    @Query("UPDATE notifications SET delivered=1 WHERE id IN (:ids)")
    suspend fun markDelivered(ids: List<Long>)

    @Query("UPDATE notifications SET skipped=1 WHERE id IN (:ids)")
    suspend fun markSkipped(ids: List<Long>)

    @Query("DELETE FROM notifications WHERE postTime < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("SELECT DISTINCT packageName FROM notifications ORDER BY packageName")
    suspend fun seenPackages(): List<String>
}
