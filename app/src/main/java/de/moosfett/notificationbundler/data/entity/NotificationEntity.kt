package de.moosfett.notificationbundler.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index("postTime"), Index("packageName")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String?,
    val packageName: String,
    val channelId: String?,
    val category: String?,
    val title: String?,
    val text: String?,
    val postTime: Long,
    val groupKey: String?,
    val isOngoing: Boolean,
    val importance: Int?,
    val extrasJson: String?,
    val delivered: Boolean = false,
    val skipped: Boolean = false,
    val critical: Boolean = false
)
