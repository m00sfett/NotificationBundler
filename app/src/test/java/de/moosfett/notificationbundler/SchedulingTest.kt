package de.moosfett.notificationbundler

import de.moosfett.notificationbundler.work.Scheduling
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SchedulingTest {

    @Test
    fun testNextRun() {
        val zone = ZoneId.of("Europe/Berlin")
        val now = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, zone)
        val times = listOf("09:00", "13:00", "18:30")

        val next = Scheduling.nextRun(now, times, zone)
        assertEquals(13, next.hour)
        assertEquals(0, next.minute)
        assertEquals(zone, next.zone)
    }
}
