package de.moosfett.notificationbundler.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String? = null,
    val channelId: String? = null,
    val keyword: String? = null,
    val isCritical: Boolean = false,
    val isExcluded: Boolean = false
)
