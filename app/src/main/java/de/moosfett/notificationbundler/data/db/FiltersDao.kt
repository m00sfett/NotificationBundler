package de.moosfett.notificationbundler.data.db

import androidx.room.*
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FiltersDao {
    @Query("SELECT * FROM filter_rules")
    suspend fun all(): List<FilterRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: FilterRuleEntity): Long

    @Delete
    suspend fun delete(rule: FilterRuleEntity)
}
