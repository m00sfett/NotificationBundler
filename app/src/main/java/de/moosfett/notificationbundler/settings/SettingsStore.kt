package de.moosfett.notificationbundler.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { _ ->
        listOf(object : DataMigration<Preferences> {
            // Convert legacy integer flags to boolean preferences
            private val oldOngoing = intPreferencesKey("includeOngoing")
            private val oldLow = intPreferencesKey("includeLowImportance")

            override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                return currentData[oldOngoing] != null || currentData[oldLow] != null
            }

            override suspend fun migrate(currentData: Preferences): Preferences {
                val mutable = currentData.toMutablePreferences()

                currentData[oldOngoing]?.let {
                    mutable[Keys.INCLUDE_ONGOING] = it == 1
                    mutable.remove(oldOngoing)
                }

                currentData[oldLow]?.let {
                    mutable[Keys.INCLUDE_LOW_IMPORTANCE] = it == 1
                    mutable.remove(oldLow)
                }

                return mutable
            }

            override suspend fun cleanUp() {}
        })
    }
)

object Keys {
    val TIMES = stringSetPreferencesKey("times") // set of "HH:mm" strings
    val RETENTION_DAYS = intPreferencesKey("retentionDays")
    val INCLUDE_ONGOING = booleanPreferencesKey("includeOngoing")
    val INCLUDE_LOW_IMPORTANCE = booleanPreferencesKey("includeLowImportance")
}

class SettingsStore(private val context: Context) {

    suspend fun getTimes(): List<String> {
        val set = context.dataStore.data.map { it[Keys.TIMES] ?: emptySet() }.first()
        return if (set.isEmpty()) listOf("09:00", "18:00") else set.sorted()
    }

    suspend fun addTime(time: String) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formatted = try {
            LocalTime.parse(time, formatter).format(formatter)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid time format: $time", e)
        }

        context.dataStore.edit { prefs ->
            val s = prefs[Keys.TIMES]?.toMutableSet() ?: mutableSetOf()
            s.add(formatted)
            prefs[Keys.TIMES] = s
        }
    }

    suspend fun removeTime(time: String) {
        context.dataStore.edit { prefs ->
            val s = prefs[Keys.TIMES]?.toMutableSet() ?: mutableSetOf()
            s.remove(time)
            prefs[Keys.TIMES] = s
        }
    }

    suspend fun retentionDays(): Int =
        context.dataStore.data.map { it[Keys.RETENTION_DAYS] ?: 30 }.first()

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { it[Keys.RETENTION_DAYS] = days }
    }

    suspend fun includeOngoing(): Boolean =
        context.dataStore.data.map { it[Keys.INCLUDE_ONGOING] ?: true }.first()

    suspend fun setIncludeOngoing(include: Boolean) {
        context.dataStore.edit { it[Keys.INCLUDE_ONGOING] = include }
    }

    suspend fun includeLowImportance(): Boolean =
        context.dataStore.data.map { it[Keys.INCLUDE_LOW_IMPORTANCE] ?: true }.first()

    suspend fun setIncludeLowImportance(include: Boolean) {
        context.dataStore.edit { it[Keys.INCLUDE_LOW_IMPORTANCE] = include }
    }
}
