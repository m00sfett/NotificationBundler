package de.moosfett.notificationbundler.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.work.Scheduling
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimezoneChangedReceiverTest {

    @Test
    fun `timezone changed schedules next delivery`() {
        val receiver = TimezoneChangedReceiver()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = Mockito.mock(SettingsStore::class.java)
        val wm = Mockito.mock(WorkManager::class.java)
        receiver.settings = settings
        receiver.workManager = wm

        Mockito.`when`(settings.getTimes()).thenReturn(emptyList())

        Mockito.mockStatic(Scheduling::class.java).use { schedStatic ->
            receiver.onReceive(context, Intent(Intent.ACTION_TIMEZONE_CHANGED))
            Thread.sleep(50)
            schedStatic.verify { Scheduling.enqueueOnce(eq(wm), anyLong()) }
        }
    }
}
