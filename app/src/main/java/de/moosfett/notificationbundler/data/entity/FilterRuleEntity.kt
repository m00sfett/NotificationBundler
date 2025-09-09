package de.moosfett.notificationbundler.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PatternType { EXACT, LIKE, REGEX }

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String? = null,
    val channelId: String? = null,
    val keyword: String? = null,
    val patternType: PatternType = PatternType.EXACT,
    val isCritical: Boolean = false,
    val isExcluded: Boolean = false,
    val isDefault: Boolean = false
)
