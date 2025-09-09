package de.moosfett.notificationbundler.data.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.moosfett.notificationbundler.data.db.AppDatabase
import de.moosfett.notificationbundler.data.entity.FilterEvaluationEntity
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FiltersRepository @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val db by lazy { AppDatabase.getInstance(appContext) }

    suspend fun all(): List<FilterRuleEntity> = db.filters().all()

    fun observeAll(): Flow<List<FilterRuleEntity>> = db.filters().observeAll()

    suspend fun upsert(rule: FilterRuleEntity) = db.filters().upsert(rule)

    suspend fun defaultForPackage(pkg: String): FilterRuleEntity? =
        db.filters().defaultForPackage(pkg)

    suspend fun logEvaluation(e: FilterEvaluationEntity) =
        db.filterEvaluations().insert(e)

    suspend fun delete(rule: FilterRuleEntity) = db.filters().delete(rule)
}
