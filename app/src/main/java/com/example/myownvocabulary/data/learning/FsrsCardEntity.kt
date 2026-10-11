package com.example.myownvocabulary.data.learning

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.model.fsrs.CardPhase
import com.example.myownvocabulary.model.fsrs.FSRS6
import com.example.myownvocabulary.model.fsrs.FlashCard
import com.example.myownvocabulary.model.fsrs.MemoryState

/** Only reviewed FSRS-6 cards are stored. A missing row represents a new card. */
@Entity(
    tableName = "fsrs_cards",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dueAt"])]
)
data class FsrsCardEntity(
    @PrimaryKey val entryId: String,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewAt: Long,
    val reviewCount: Int,
    val lapseCount: Int,
    val phase: String,
    val learningStep: Int?,
    val interval: Int,
    val algorithmVersion: Int
) {
    fun toCard(): FlashCard {
        require(algorithmVersion == FSRS6.ALGORITHM_VERSION) { "Replay history to migrate the FSRS model" }
        return FlashCard(
            id = entryId,
            memory = MemoryState(stability, difficulty),
            dueAt = dueAt,
            lastReviewAt = lastReviewAt,
            reviewCount = reviewCount,
            lapseCount = lapseCount,
            phase = CardPhase.valueOf(phase),
            learningStep = learningStep,
            interval = interval
        )
    }

    companion object {
        fun fromCard(card: FlashCard): FsrsCardEntity {
            val memory = requireNotNull(card.memory)
            return FsrsCardEntity(
                card.id, memory.stability, memory.difficulty, card.dueAt,
                requireNotNull(card.lastReviewAt), card.reviewCount, card.lapseCount,
                card.phase.name, card.learningStep, card.interval, FSRS6.ALGORITHM_VERSION
            )
        }
    }
}
