package de.moosfett.notificationbundler.data.db

import androidx.room.*
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FiltersDao {
    @Query("SELECT * FROM filter_rules ORDER BY id")
    suspend fun all(): List<FilterRuleEntity>

    @Query("SELECT * FROM filter_rules ORDER BY id")
    fun observeAll(): Flow<List<FilterRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: FilterRuleEntity): Long

    @Query("SELECT * FROM filter_rules WHERE packageName = :pkg AND isDefault = 1 LIMIT 1")
    suspend fun defaultForPackage(pkg: String): FilterRuleEntity?

    @Delete
    suspend fun delete(rule: FilterRuleEntity)
}
