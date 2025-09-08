package de.moosfett.notificationbundler.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the user preferences for the settings screen.
 */
data class SettingsUiState(
    val includeOngoing: Boolean = true,
    val includeLowImportance: Boolean = true,
    val retentionDays: Int = 30,
)

/**
 * Simple ViewModel that proxies the [SettingsStore].
 */
class SettingsViewModel(private val store: SettingsStore) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            _state.value = SettingsUiState(
                includeOngoing = store.includeOngoing(),
                includeLowImportance = store.includeLowImportance(),
                retentionDays = store.retentionDays(),
            )
        }
    }

    fun setIncludeOngoing(include: Boolean) {
        _state.update { it.copy(includeOngoing = include) }
        viewModelScope.launch { store.setIncludeOngoing(include) }
    }

    fun setIncludeLowImportance(include: Boolean) {
        _state.update { it.copy(includeLowImportance = include) }
        viewModelScope.launch { store.setIncludeLowImportance(include) }
    }

    fun setRetentionDays(days: Int) {
        _state.update { it.copy(retentionDays = days) }
        viewModelScope.launch { store.setRetentionDays(days) }
    }
}

/**
 * Factory to create a [SettingsViewModel] with a [SettingsStore].
 */
class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsStore(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
