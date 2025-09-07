package de.moosfett.notificationbundler.receivers

import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryActionReceiverTest {

    @Test
    fun `deliver now enqueues work`() {
        val receiver = DeliveryActionReceiver()
        val context = mock(Context::class.java)
        val wm = mock(WorkManager::class.java)
        Mockito.mockStatic(WorkManager::class.java).use { wmStatic ->
            wmStatic.`when`<WorkManager> { WorkManager.getInstance(context) }.thenReturn(wm)

            receiver.onReceive(context, Intent(DeliveryActionReceiver.ACTION_DELIVER_NOW))

            verify(wm).enqueueUniqueWork(eq("delivery"), eq(ExistingWorkPolicy.KEEP), any(OneTimeWorkRequest::class.java))
        }
    }

    @Test
    fun `snooze 15m schedules delayed work`() = runBlocking {
        val scheduler = TestCoroutineScheduler()
        val dispatcher = StandardTestDispatcher(scheduler)
        val receiver = DeliveryActionReceiver(dispatcher)
        val context = mock(Context::class.java)
        Mockito.mockStatic(Scheduling::class.java).use { schedStatic ->
            receiver.onReceive(context, Intent(DeliveryActionReceiver.ACTION_SNOOZE_15M))
            scheduler.advanceUntilIdle()
            schedStatic.verify { Scheduling.enqueueOnce(context, 15L * 60L * 1000L) }
        }
    }

    @Test
    fun `skip does nothing`() {
        val receiver = DeliveryActionReceiver()
        val context = mock(Context::class.java)
        Mockito.mockStatic(WorkManager::class.java).use { wmStatic ->
            Mockito.mockStatic(Scheduling::class.java).use { schedStatic ->
                receiver.onReceive(context, Intent(DeliveryActionReceiver.ACTION_SKIP))
                wmStatic.verifyNoInteractions()
                schedStatic.verifyNoInteractions()
            }
        }
    }
}
