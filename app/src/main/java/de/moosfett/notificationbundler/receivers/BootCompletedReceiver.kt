package de.moosfett.notificationbundler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                try {
                    scheduleNextDelivery(context)
                } finally {
                    pendingResult.finish()
                }
            }.invokeOnCompletion { scope.cancel() }
        }
    }
}
