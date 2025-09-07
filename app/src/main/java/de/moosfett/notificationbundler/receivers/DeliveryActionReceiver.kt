package de.moosfett.notificationbundler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.moosfett.notificationbundler.work.Scheduling
import de.moosfett.notificationbundler.work.DeliveryWorker
import androidx.work.*

class DeliveryActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DELIVER_NOW -> {
                // Enqueue an immediate run
                val req = OneTimeWorkRequestBuilder<DeliveryWorker>().build()
                WorkManager.getInstance(context).enqueue(req)
            }
            ACTION_SNOOZE_15M -> {
                // Replace any existing with a new one in 15m
                Scheduling.enqueueOnce(context, 15L * 60L * 1000L)
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
