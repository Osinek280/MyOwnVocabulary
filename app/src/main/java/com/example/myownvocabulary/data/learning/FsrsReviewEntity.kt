package com.example.myownvocabulary.data.learning

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.model.fsrs.Rating
import com.example.myownvocabulary.model.fsrs.ReviewLog

@Entity(
    tableName = "fsrs_reviews",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entryId", "reviewedAt"])]
)
data class FsrsReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: String,
    val rating: Int,
    val reviewedAt: Long,
    val elapsedDays: Long,
    val algorithmVersion: Int
) {
    fun toLog(): ReviewLog = ReviewLog(
        entryId,
        Rating.entries.single { it.value == rating },
        reviewedAt,
        elapsedDays,
        algorithmVersion
    )

    companion object {
        fun fromLog(log: ReviewLog): FsrsReviewEntity = FsrsReviewEntity(
            entryId = log.cardId,
            rating = log.rating.value,
            reviewedAt = log.reviewedAt,
            elapsedDays = log.elapsedDays,
            algorithmVersion = log.algorithmVersion
        )
    }
}
