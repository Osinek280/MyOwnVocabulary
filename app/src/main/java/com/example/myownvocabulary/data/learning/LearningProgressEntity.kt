package com.example.myownvocabulary.data.learning

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myownvocabulary.data.entry.EntryEntity

/**
 * Current learning state of one vocabulary entry.
 *
 * An entry without a progress row is considered new. The row is removed together
 * with its vocabulary entry, while aggregate session history is kept separately.
 */
@Entity(
    tableName = "learning_progress",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["nextReviewAt"])]
)
data class LearningProgressEntity(
    @PrimaryKey val entryId: String,
    val status: LearningStatus = LearningStatus.LEARNING,
    val stage: Int = 0,
    val correctStreak: Int = 0,
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null
)

enum class LearningStatus {
    NEW,
    LEARNING,
    REVIEW,
    RELEARNING
}
