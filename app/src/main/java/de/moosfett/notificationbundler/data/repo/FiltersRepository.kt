package de.moosfett.notificationbundler.data.repo

import android.content.Context
import de.moosfett.notificationbundler.data.db.AppDatabase
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

class FiltersRepository(private val appContext: Context) {
    private val db by lazy { AppDatabase.getInstance(appContext) }

    suspend fun all(): List<FilterRuleEntity> = db.filters().all()

    fun observeAll(): Flow<List<FilterRuleEntity>> = db.filters().observeAll()

    suspend fun upsert(rule: FilterRuleEntity) = db.filters().upsert(rule)

    suspend fun delete(rule: FilterRuleEntity) = db.filters().delete(rule)
}
