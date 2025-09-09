package de.moosfett.notificationbundler.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import de.moosfett.notificationbundler.data.repo.FiltersRepository
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
import de.moosfett.notificationbundler.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class NotificationCollectorServiceTest {
    private val service = NotificationCollectorService()
    private val notificationsRepo = mock(NotificationsRepository::class.java)
    private val filtersRepo = mock(FiltersRepository::class.java)
    private val settings = mock(SettingsStore::class.java)
    private val notificationManager = mock(NotificationManager::class.java)
    private val includeOngoingFlow = MutableStateFlow(true)
    private val includeLowImportanceFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        setField("scope", CoroutineScope(Dispatchers.Unconfined))
        setField("notificationsRepo", notificationsRepo)
        setField("filtersRepo", filtersRepo)
        setField("notificationManager", notificationManager)
        setField("includeOngoingFlow", includeOngoingFlow)
        setField("includeLowImportanceFlow", includeLowImportanceFlow)
        setField("settings", settings)
    }

    @Test
    fun ongoingNotificationSkippedWhenDisabled() = runBlocking {
        includeOngoingFlow.value = false
        includeLowImportanceFlow.value = true

        val sbn = mockSbn(isOngoing = true, importance = NotificationManager.IMPORTANCE_DEFAULT)

        service.onNotificationPosted(sbn)

        verify(notificationsRepo, never()).insert(ArgumentMatchers.any(NotificationEntity::class.java))
    }

    @Test
    fun lowImportanceNotificationSkippedWhenDisabled() = runBlocking {
        includeOngoingFlow.value = true
        includeLowImportanceFlow.value = false

        val sbn = mockSbn(isOngoing = false, importance = NotificationManager.IMPORTANCE_LOW)

        service.onNotificationPosted(sbn)

        verify(notificationsRepo, never()).insert(ArgumentMatchers.any(NotificationEntity::class.java))
    }

    private fun mockSbn(isOngoing: Boolean, importance: Int): StatusBarNotification {
        val extras = Bundle().apply {
            putCharSequence("android.title", "title")
            putCharSequence("android.text", "text")
        }
        val n = mock(Notification::class.java)
        Mockito.`when`(n.extras).thenReturn(extras)
        Mockito.`when`(n.channelId).thenReturn("chan")
        Mockito.`when`(n.category).thenReturn("cat")
        val channel = NotificationChannel("chan", "chan", importance)
        Mockito.`when`(notificationManager.getNotificationChannel("chan")).thenReturn(channel)

        val sbn = mock(StatusBarNotification::class.java)
        Mockito.`when`(sbn.packageName).thenReturn("pkg")
        Mockito.`when`(sbn.key).thenReturn("key")
        Mockito.`when`(sbn.notification).thenReturn(n)
        Mockito.`when`(sbn.isOngoing).thenReturn(isOngoing)
        Mockito.`when`(sbn.postTime).thenReturn(0L)
        Mockito.`when`(sbn.groupKey).thenReturn("group")
        return sbn
    }

    private fun setField(name: String, value: Any) {
        val f = NotificationCollectorService::class.java.getDeclaredField(name)
        f.isAccessible = true
        f.set(service, value)
    }
}
