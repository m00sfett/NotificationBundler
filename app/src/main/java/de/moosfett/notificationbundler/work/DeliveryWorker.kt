package de.moosfett.notificationbundler.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
import de.moosfett.notificationbundler.notifications.Notifier
import de.moosfett.notificationbundler.settings.SettingsStore
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.ZonedDateTime

class DeliveryWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {
    private val notifications = NotificationsRepository(appContext)
    private val settings = SettingsStore(appContext)

    override suspend fun doWork(): Result {
        // 1) Query pending notifications
        val pending = notifications.pending()

        if (pending.isNotEmpty()) {
            // 2) Build simple per-app lines
            val lines = pending.groupBy { it.packageName }
                .map { (pkg, list) -> "${list.size} × $pkg" }
                .sorted()

            // 3) Post summary notification
            Notifier.notifyBundledSummary(applicationContext, lines)

            // 4) Mark delivered
            notifications.markDelivered(pending.map { it.id })
        }

        // 5) Housekeeping: retention
        val days = settings.retentionDays()
        val threshold = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        notifications.deleteOlderThan(threshold)

        // 6) Reschedule next run
        rescheduleNext()

        return Result.success()
    }

    private fun rescheduleNext() {
        runBlocking {
            val times = settings.getTimes()
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val next = Scheduling.nextRun(now, times)
            val delay = next.toInstant().toEpochMilli() - System.currentTimeMillis()
            Scheduling.enqueueOnce(applicationContext, delay)
        }
    }
}
