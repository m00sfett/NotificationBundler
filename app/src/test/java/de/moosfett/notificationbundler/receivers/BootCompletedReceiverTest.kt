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
class BootCompletedReceiverTest {

    @Test
    fun `boot completed schedules next delivery`() {
        val receiver = BootCompletedReceiver()
        val context = ApplicationProvider.getApplicationContext<Context>()

        Mockito.mockConstruction(SettingsStore::class.java) { mock, _ ->
            Mockito.`when`(mock.getTimes()).thenReturn(emptyList())
        }.use {
            Mockito.mockStatic(Scheduling::class.java).use { schedStatic ->
                receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
                Thread.sleep(50)
                schedStatic.verify { Scheduling.enqueueOnce(eq(context), anyLong()) }
            }
        }
    }
}
