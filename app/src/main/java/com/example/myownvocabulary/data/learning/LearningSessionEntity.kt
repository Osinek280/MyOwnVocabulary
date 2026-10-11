package com.example.myownvocabulary.data.learning

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Aggregate history of a quiz session.
 *
 * It intentionally has no foreign key to an entry, so deleting vocabulary does
 * not erase streak, calendar, or historical activity data.
 */
@Entity(
    tableName = "learning_sessions",
    indices = [Index(value = ["completedAt"])]
)
data class LearningSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: LearningSessionType,
    val startedAt: Long,
    val completedAt: Long? = null,
    val plannedCount: Int,
    val answeredCount: Int = 0,
    val correctCount: Int = 0
)

enum class LearningSessionType {
    DAILY,
    QUICK_REVIEW,
    CONSOLIDATE,
    REVIEW_NEW
}
