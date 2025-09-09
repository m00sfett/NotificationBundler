package de.moosfett.notificationbundler.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
import de.moosfett.notificationbundler.settings.SettingsStore
import de.moosfett.notificationbundler.ui.screens.PreviewEvent
import de.moosfett.notificationbundler.ui.screens.PreviewState
import de.moosfett.notificationbundler.work.Scheduling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

/** ViewModel backing the preview screen. */
class PreviewViewModel(
    private val repo: NotificationsRepository,
    private val settings: SettingsStore,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PreviewState())
    val state: StateFlow<PreviewState> = _state

    init {
        viewModelScope.launch { refresh() }
    }

    fun onEvent(event: PreviewEvent) {
        when (event) {
            PreviewEvent.DeliverNow -> scheduleOnce(0)
            PreviewEvent.Snooze15m -> scheduleOnce(15L * 60L * 1000L)
            PreviewEvent.Skip -> viewModelScope.launch {
                val ids = repo.pending().map { it.id }
                if (ids.isNotEmpty()) repo.markSkipped(ids)
                scheduleFromSettings()
                refresh()
            }
        }
    }

    private fun scheduleOnce(delay: Long) {
        viewModelScope.launch { Scheduling.enqueueOnce(workManager, delay) }
    }

    private suspend fun scheduleFromSettings() {
        val times = settings.getTimes()
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = Scheduling.nextRun(now, times)
        val delay = (next.toInstant().toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0)
        Scheduling.enqueueOnce(workManager, delay)
    }

    private suspend fun refresh() {
        val lines = repo.pending()
            .groupBy { it.packageName }
            .map { (pkg, list) -> "${list.size} × $pkg" }
            .sorted()
        _state.update { it.copy(lines = lines) }
    }
}

/** Factory to create a [PreviewViewModel] with context backed dependencies. */
class PreviewViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreviewViewModel(
                NotificationsRepository(context.applicationContext),
                SettingsStore(context.applicationContext),
                WorkManager.getInstance(context.applicationContext),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

