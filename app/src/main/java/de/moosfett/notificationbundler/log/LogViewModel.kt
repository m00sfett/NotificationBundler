package de.moosfett.notificationbundler.log

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.moosfett.notificationbundler.data.repo.DeliveryLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** UI state for the log screen. */
data class LogUiState(val entries: List<LogEntry> = emptyList())

/** A single row in the log list. */
data class LogEntry(val id: Long, val time: String, val count: Int)

/** ViewModel backing the log screen. */
class LogViewModel(private val repo: DeliveryLogRepository) : ViewModel() {
    private val _state = MutableStateFlow(LogUiState())
    val state: StateFlow<LogUiState> = _state

    private val formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm")

    init {
        viewModelScope.launch {
            repo.logs().collect { list ->
                val entries = list.map {
                    LogEntry(
                        id = it.id,
                        time = Instant.ofEpochMilli(it.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(formatter),
                        count = it.deliveredCount
                    )
                }
                _state.value = LogUiState(entries)
            }
        }
    }

    fun clear() {
        viewModelScope.launch { repo.clear() }
    }
}

/** Factory to create [LogViewModel] with context backed repository. */
class LogViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogViewModel(DeliveryLogRepository(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
