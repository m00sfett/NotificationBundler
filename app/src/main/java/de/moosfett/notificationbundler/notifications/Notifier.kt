package de.moosfett.notificationbundler.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.moosfett.notificationbundler.R
import de.moosfett.notificationbundler.ui.MainActivity
import de.moosfett.notificationbundler.receivers.DeliveryActionReceiver

object Notifier {

    const val CH_BUNDLED = "bundled"
    const val CH_CRITICAL = "critical"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CH_BUNDLED, "Gebündelte Nachrichten", NotificationManager.IMPORTANCE_DEFAULT)
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_CRITICAL, "Kritische Nachrichten", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun notifyCritical(context: Context, title: String?, text: String?, sourceLabel: String): Int {
        val pi = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CH_CRITICAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title ?: sourceLabel)
            .setContentText(text ?: sourceLabel)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        val id = (System.currentTimeMillis() % 100000).toInt()
        NotificationManagerCompat.from(context).notify(id, n)
        return id
    }

    fun notifyBundledSummary(context: Context, lines: List<String>): Int {
        val pi = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliverNow = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, DeliveryActionReceiver::class.java).setAction(DeliveryActionReceiver.ACTION_DELIVER_NOW),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze15m = PendingIntent.getBroadcast(
            context, 2,
            Intent(context, DeliveryActionReceiver::class.java).setAction(DeliveryActionReceiver.ACTION_SNOOZE_15M),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skip = PendingIntent.getBroadcast(
            context, 3,
            Intent(context, DeliveryActionReceiver::class.java).setAction(DeliveryActionReceiver.ACTION_SKIP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val inbox = NotificationCompat.InboxStyle()
        lines.take(7).forEach { inbox.addLine(it) }
        val n = NotificationCompat.Builder(context, CH_BUNDLED)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Gebündelte Benachrichtigungen: ${lines.size}")
            .setStyle(inbox)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_play,
                context.getString(R.string.deliver_now),
                deliverNow
            )
            .addAction(
                android.R.drawable.ic_lock_idle_alarm,
                context.getString(R.string.snooze_15m),
                snooze15m
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.skip),
                skip
            )
            .build()
        val id = 1010
        NotificationManagerCompat.from(context).notify(id, n)
        return id
    }
}
