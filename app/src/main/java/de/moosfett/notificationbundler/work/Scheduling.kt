package de.moosfett.notificationbundler.work

import android.content.Context
import androidx.work.*
import androidx.work.await
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Scheduling {
    private const val TAG = "delivery"

    fun nextRun(now: ZonedDateTime, times: List<String>, zone: ZoneId = now.zone): ZonedDateTime {
        val parsed = times.mapNotNull {
            try { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { null }
        }.sorted()
        if (parsed.isEmpty()) return now.plusHours(1) // fallback

        val today = now.toLocalDate()
        parsed.forEach { t ->
            val candidate = ZonedDateTime.of(today, t, zone)
            if (candidate.isAfter(now)) return candidate
        }
        // otherwise tomorrow at the earliest time
        return ZonedDateTime.of(today.plusDays(1), parsed.first(), zone)
    }

    suspend fun enqueueOnce(context: Context, delayMillis: Long) {
        val wm = WorkManager.getInstance(context)
        val existing = try { wm.getWorkInfosByTag(TAG).await() } catch (_: Exception) { emptyList() }
        val hasRunning = existing.any { it.state == WorkInfo.State.RUNNING }
        if (hasRunning) return

        val clampedDelay = delayMillis.coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<DeliveryWorker>()
            .setInitialDelay(clampedDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.NONE)
            .addTag(TAG)
            .build()
        wm.enqueueUniqueWork(
            "delivery",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}
