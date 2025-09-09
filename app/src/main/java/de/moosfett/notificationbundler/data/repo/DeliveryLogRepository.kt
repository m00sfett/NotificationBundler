package de.moosfett.notificationbundler.data.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.moosfett.notificationbundler.data.db.AppDatabase
import de.moosfett.notificationbundler.data.entity.DeliveryLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Repository access to the delivery log table. */
@Singleton
class DeliveryLogRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val db by lazy { AppDatabase.getInstance(appContext) }

    fun logs(): Flow<List<DeliveryLogEntity>> = db.deliveryLog().all()

    suspend fun insert(timestamp: Long, count: Int) {
        db.deliveryLog().insert(DeliveryLogEntity(timestamp = timestamp, deliveredCount = count))
    }

    suspend fun clear() = db.deliveryLog().clear()
}
