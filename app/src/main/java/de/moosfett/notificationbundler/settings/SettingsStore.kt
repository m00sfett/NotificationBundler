package de.moosfett.notificationbundler.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object Keys {
    val TIMES = stringSetPreferencesKey("times") // set of "HH:mm" strings
    val RETENTION_DAYS = intPreferencesKey("retentionDays")
    val INCLUDE_ONGOING = intPreferencesKey("includeOngoing") // 0/1
    val INCLUDE_LOW_IMPORTANCE = intPreferencesKey("includeLowImportance") // 0/1
}

class SettingsStore(private val context: Context) {

    suspend fun getTimes(): List<String> {
        val set = context.dataStore.data.map { it[Keys.TIMES] ?: emptySet() }.first()
        return if (set.isEmpty()) listOf("09:00", "18:00") else set.sorted()
    }

    suspend fun addTime(time: String) {
        context.dataStore.edit { prefs ->
            val s = prefs[Keys.TIMES]?.toMutableSet() ?: mutableSetOf()
            s.add(time)
            prefs[Keys.TIMES] = s
        }
    }

    suspend fun retentionDays(): Int =
        context.dataStore.data.map { it[Keys.RETENTION_DAYS] ?: 30 }.first()

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { it[Keys.RETENTION_DAYS] = days }
    }

    suspend fun includeOngoing(): Boolean =
        context.dataStore.data.map { (it[Keys.INCLUDE_ONGOING] ?: 1) == 1 }.first()

    suspend fun setIncludeOngoing(include: Boolean) {
        context.dataStore.edit { it[Keys.INCLUDE_ONGOING] = if (include) 1 else 0 }
    }

    suspend fun includeLowImportance(): Boolean =
        context.dataStore.data.map { (it[Keys.INCLUDE_LOW_IMPORTANCE] ?: 1) == 1 }.first()

    suspend fun setIncludeLowImportance(include: Boolean) {
        context.dataStore.edit { it[Keys.INCLUDE_LOW_IMPORTANCE] = if (include) 1 else 0 }
    }
}
