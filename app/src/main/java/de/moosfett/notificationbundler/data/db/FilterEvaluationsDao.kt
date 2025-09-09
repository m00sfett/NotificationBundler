package de.moosfett.notificationbundler.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import de.moosfett.notificationbundler.data.entity.FilterEvaluationEntity

@Dao
interface FilterEvaluationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: FilterEvaluationEntity)
}
