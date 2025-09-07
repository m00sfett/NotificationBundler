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
        NotificationManagerCompat.from(context).notify((System.currentTimeMillis()%100000).toInt(), n)
        return 1
    }

    fun notifyBundledSummary(context: Context, lines: List<String>): Int {
        val pi = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
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
            .build()
        val id = 1010
        NotificationManagerCompat.from(context).notify(id, n)
        return id
    }
}
