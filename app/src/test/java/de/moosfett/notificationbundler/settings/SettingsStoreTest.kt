package de.moosfett.notificationbundler.settings

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SettingsStoreTest {
    private val context = RuntimeEnvironment.getApplication()
    private val store = SettingsStore(context)

    @Test
    fun `addTime accepts valid time`() = runBlocking {
        val t = "12:34"
        store.addTime(t)
        val times = store.getTimes()
        assertTrue(times.contains(t))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `addTime rejects invalid time`() = runBlocking {
        store.addTime("99:99")
    }
}
