package de.moosfett.notificationbundler.receivers

import androidx.work.WorkManager
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.work.Scheduling
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

/**
 * Helper functions for broadcast receivers.
 */
suspend fun scheduleNextDelivery(settings: SettingsStore, workManager: WorkManager) {
    val times = settings.getTimes()
    val now = ZonedDateTime.now(ZoneId.systemDefault())
    val next = Scheduling.nextRun(now, times)
    val delay = max(0L, next.toInstant().toEpochMilli() - System.currentTimeMillis())
    Scheduling.enqueueOnce(workManager, delay)
}
