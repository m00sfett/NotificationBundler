package de.moosfett.notificationbundler

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import de.moosfett.notificationbundler.notifications.Notifier
import de.moosfett.notificationbundler.receivers.scheduleNextDelivery
import de.moosfett.notificationbundler.settings.SettingsStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotificationBundlerApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settings: SettingsStore

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Ensure notification channels exist at startup.
        Notifier.ensureChannels(this)
        CoroutineScope(Dispatchers.Default).launch {
            val wm = WorkManager.getInstance(this@NotificationBundlerApp)
            val existing = try {
                wm.getWorkInfosForUniqueWork("delivery").await()
            } catch (_: Exception) {
                emptyList()
            }
            val hasQueued = existing.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
            if (!hasQueued) {
                scheduleNextDelivery(settings, wm)
            }
        }
    }
}
