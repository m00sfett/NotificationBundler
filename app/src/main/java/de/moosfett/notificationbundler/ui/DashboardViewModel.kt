package de.moosfett.notificationbundler.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.ui.screens.DashboardEvent
import de.moosfett.notificationbundler.ui.screens.DashboardState
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** ViewModel backing the dashboard screen. */
class DashboardViewModel(
    private val repo: NotificationsRepository,
    private val settings: SettingsStore,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        viewModelScope.launch {
            combine(
                repo.countToday(),
                repo.pendingCount(),
                repo.criticalCount(),
            ) { today, pending, critical -> Triple(today, pending, critical) }
                .collect { (today, pending, critical) ->
                    _state.update {
                        it.copy(
                            todayCount = today,
                            pendingCount = pending,
                            criticalCount = critical,
                        )
                    }
                }
        }

        viewModelScope.launch {
            settings.handlingActiveFlow.collect { active ->
                _state.update { it.copy(handlingActive = active) }
            }
        }
        viewModelScope.launch { updateFromSettings(schedule = false) }
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.DeliverNow -> scheduleOnce(0)
            DashboardEvent.Snooze15m -> scheduleOnce(15L * 60L * 1000L)
            DashboardEvent.Skip -> viewModelScope.launch {
                val ids = repo.pending().map { it.id }
                if (ids.isNotEmpty()) repo.markSkipped(ids)
                updateFromSettings(schedule = true)
            }
            DashboardEvent.PostNotificationsGranted -> viewModelScope.launch {
                updateFromSettings(schedule = true)
            }
            is DashboardEvent.ToggleHandling -> viewModelScope.launch {
                settings.setHandlingActive(event.enabled)
            }
        }
    }

    private fun scheduleOnce(delay: Long) {
        viewModelScope.launch {
            Scheduling.enqueueOnce(workManager, delay)
            val time = Instant.ofEpochMilli(System.currentTimeMillis() + delay)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
            _state.update { it.copy(nextDeliveryTime = time) }
        }
    }

    private suspend fun updateFromSettings(schedule: Boolean) {
        val times = settings.getTimes()
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = Scheduling.nextRun(now, times)
        val delay = (next.toInstant().toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0)
        if (schedule) Scheduling.enqueueOnce(workManager, delay)
        val formatted = next.toLocalTime().format(formatter)
        _state.update { it.copy(nextDeliveryTime = formatted) }
    }
}

/** Factory to create a [DashboardViewModel] with context backed dependencies. */
class DashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                NotificationsRepository(context.applicationContext),
                SettingsStore(context.applicationContext),
                WorkManager.getInstance(context.applicationContext),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

