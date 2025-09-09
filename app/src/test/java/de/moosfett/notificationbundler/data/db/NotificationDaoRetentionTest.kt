package de.moosfett.notificationbundler.data.db

import androidx.room.Room
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NotificationDaoRetentionTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: NotificationDao

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.notifications()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun notificationAt(time: Long) = NotificationEntity(
        key = null,
        packageName = "pkg",
        channelId = null,
        category = null,
        title = null,
        text = null,
        postTime = time,
        groupKey = null,
        isOngoing = false,
        importance = null,
        extrasJson = null,
    )

    @Test
    fun `deleteOlderThan removes only outside retention`() = runTest {
        val dayMs = 24L * 60L * 60L * 1000L
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2024, 1, 5, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
        val threshold = now - 2 * dayMs

        val times = listOf(
            now - 3 * dayMs,     // 3 days old -> removed
            threshold - 1,       // just before threshold -> removed
            threshold,           // exactly at threshold -> kept
            now - dayMs,         // within retention -> kept
            now                  // newest -> kept
        )

        times.forEach { dao.insert(notificationAt(it)) }

        dao.deleteOlderThan(threshold)

        val remaining = dao.pending().map { it.postTime }
        assertEquals(listOf(threshold, now - dayMs, now), remaining)
    }

    @Test
    fun `deleteOlderThan crosses daylight saving`() = runTest {
        val zone = ZoneId.of("America/New_York")
        val dayMs = 24L * 60L * 60L * 1000L
        val now = ZonedDateTime.of(2024, 3, 10, 5, 0, 0, 0, zone).toInstant().toEpochMilli()
        val threshold = now - dayMs

        val before = threshold - 1
        val after = threshold + 1

        dao.insert(notificationAt(before))
        dao.insert(notificationAt(after))

        dao.deleteOlderThan(threshold)

        val remaining = dao.pending().map { it.postTime }
        assertEquals(listOf(after), remaining)
    }

    @Test
    fun `deleteOlderThan unaffected by timezone change`() = runTest {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val dayMs = 24L * 60L * 60L * 1000L
            val zoneA = ZoneId.systemDefault()
            val older = ZonedDateTime.of(2024, 6, 1, 12, 0, 0, 0, zoneA).toInstant().toEpochMilli()
            val newer = ZonedDateTime.of(2024, 6, 2, 12, 0, 0, 0, zoneA).toInstant().toEpochMilli()
            dao.insert(notificationAt(older))
            dao.insert(notificationAt(newer))

            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            val zoneB = ZoneId.systemDefault()
            val now = ZonedDateTime.of(2024, 6, 3, 12, 0, 0, 0, zoneB).toInstant().toEpochMilli()
            val threshold = now - dayMs

            dao.deleteOlderThan(threshold)
            val remaining = dao.pending().map { it.postTime }
            assertEquals(listOf(newer), remaining)
        } finally {
            TimeZone.setDefault(original)
        }
    }
}

