package de.moosfett.notificationbundler.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
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

        Mockito.mockConstruction(SettingsStore::class.java) { mock, _ ->
            Mockito.`when`(mock.getTimes()).thenReturn(emptyList())
        }.use {
            Mockito.mockStatic(Scheduling::class.java).use { schedStatic ->
                receiver.onReceive(context, Intent(Intent.ACTION_TIMEZONE_CHANGED))
                Thread.sleep(50)
                schedStatic.verify { Scheduling.enqueueOnce(eq(context), anyLong()) }
            }
        }
    }
}
