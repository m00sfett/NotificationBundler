package de.moosfett.notificationbundler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import de.moosfett.notificationbundler.work.DeliveryWorker
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DeliveryActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DELIVER_NOW -> {
                // Enqueue an immediate run
                val req = OneTimeWorkRequestBuilder<DeliveryWorker>().build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "delivery",
                    ExistingWorkPolicy.KEEP,
                    req
                )
            }
            ACTION_SNOOZE_15M -> {
                // Replace any existing with a new one in 15m
                val pendingResult = goAsync()
                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    try {
                        val delay = 15 * 60 * 1000L
                        Scheduling.enqueueOnce(context, delay)
                    } finally {
                        pendingResult.finish()
                    }
                }.invokeOnCompletion { scope.cancel() }
            }
            ACTION_SKIP -> {
                // Do nothing; next regular run will happen
            }
        }
    }

    companion object {
        const val ACTION_DELIVER_NOW = "de.moosfett.notificationbundler.ACTION_DELIVER_NOW"
        const val ACTION_SNOOZE_15M = "de.moosfett.notificationbundler.ACTION_SNOOZE_15M"
        const val ACTION_SKIP = "de.moosfett.notificationbundler.ACTION_SKIP"
    }
}
