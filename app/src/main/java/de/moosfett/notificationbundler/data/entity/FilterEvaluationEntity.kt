package de.moosfett.notificationbundler.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "filter_evaluations",
    indices = [Index("postTime"), Index("packageName")]
)
data class FilterEvaluationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationKey: String?,
    val packageName: String,
    val postTime: Long,
    val ruleId: Long?,
    val decision: String
)
