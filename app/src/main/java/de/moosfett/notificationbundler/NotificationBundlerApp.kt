package de.moosfett.notificationbundler

import android.app.Application
import de.moosfett.notificationbundler.notifications.Notifier
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationBundlerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ensure notification channels exist at startup.
        Notifier.ensureChannels(this)
    }
}
