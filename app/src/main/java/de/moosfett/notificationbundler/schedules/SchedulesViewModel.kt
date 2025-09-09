package de.moosfett.notificationbundler.schedules

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import de.moosfett.notificationbundler.receivers.scheduleNextDelivery
import de.moosfett.notificationbundler.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the configured delivery times.
 */
data class SchedulesUiState(
    val times: List<String> = emptyList()
)

class SchedulesViewModel(
    private val context: Context,
    private val store: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SchedulesUiState())
    val state: StateFlow<SchedulesUiState> = _state

    init {
        viewModelScope.launch {
            _state.value = SchedulesUiState(store.getTimes())
        }
    }

    fun addTime(hour: Int, minute: Int) {
        val time = String.format("%02d:%02d", hour, minute)
        viewModelScope.launch {
            store.addTime(time)
            scheduleNextDelivery(store, WorkManager.getInstance(context))
            _state.update { it.copy(times = store.getTimes()) }
        }
    }

    fun removeTime(time: String) {
        viewModelScope.launch {
            store.removeTime(time)
            scheduleNextDelivery(store, WorkManager.getInstance(context))
            _state.update { it.copy(times = store.getTimes()) }
        }
    }
}

class SchedulesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchedulesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchedulesViewModel(
                context.applicationContext,
                SettingsStore(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
