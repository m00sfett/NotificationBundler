package de.moosfett.notificationbundler.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.moosfett.notificationbundler.data.entity.DeliveryLogEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the delivery log table. */
@Dao
interface DeliveryLogDao {
    @Insert
    suspend fun insert(e: DeliveryLogEntity)

    @Query("SELECT * FROM delivery_log ORDER BY timestamp DESC")
    fun all(): Flow<List<DeliveryLogEntity>>

    @Query("DELETE FROM delivery_log")
    suspend fun clear()
}
