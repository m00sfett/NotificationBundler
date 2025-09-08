package de.moosfett.notificationbundler.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Persists a single delivery run.
 * Stores the timestamp (epoch millis) and how many notifications were delivered.
 */
@Entity(
    tableName = "delivery_log",
    indices = [Index("timestamp")]
)
data class DeliveryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val deliveredCount: Int
)
