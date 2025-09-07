package de.moosfett.notificationbundler.receivers

import android.content.Context
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.work.Scheduling
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

/**
 * Helper functions for broadcast receivers.
 */
suspend fun scheduleNextDelivery(context: Context) {
    val settings = SettingsStore(context)
    val times = settings.getTimes()
    val now = ZonedDateTime.now(ZoneId.systemDefault())
    val next = Scheduling.nextRun(now, times)
    val delay = max(0L, next.toInstant().toEpochMilli() - System.currentTimeMillis())
    Scheduling.enqueueOnce(context, delay)
}
