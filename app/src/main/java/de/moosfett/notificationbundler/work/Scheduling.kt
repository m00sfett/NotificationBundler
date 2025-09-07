package de.moosfett.notificationbundler.work

import android.content.Context
import androidx.work.*
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object Scheduling {

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

    fun enqueueOnce(context: Context, delayMillis: Long) {
        val req = OneTimeWorkRequestBuilder<DeliveryWorker>()
            .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.NONE)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "delivery",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}
