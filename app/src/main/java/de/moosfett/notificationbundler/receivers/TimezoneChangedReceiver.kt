package de.moosfett.notificationbundler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.ZonedDateTime

class TimezoneChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            runBlocking {
                val settings = SettingsStore(context)
                val times = settings.getTimes()
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                val next = Scheduling.nextRun(now, times)
                val delay = next.toInstant().toEpochMilli() - System.currentTimeMillis()
                Scheduling.enqueueOnce(context, delay)
            }
        }
    }
}
