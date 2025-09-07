package de.moosfett.notificationbundler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                try {
                    val settings = SettingsStore(context)
                    val times = settings.getTimes()
                    val now = ZonedDateTime.now(ZoneId.systemDefault())
                    val next = Scheduling.nextRun(now, times)
                    val delay =
                        max(0L, next.toInstant().toEpochMilli() - System.currentTimeMillis())
                    Scheduling.enqueueOnce(context, delay)
                } finally {
                    pendingResult.finish()
                }
            }.invokeOnCompletion { scope.cancel() }
        }
    }
}
