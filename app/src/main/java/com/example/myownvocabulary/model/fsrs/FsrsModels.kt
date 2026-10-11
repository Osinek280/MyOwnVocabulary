package com.example.myownvocabulary.model.fsrs

enum class Rating(val value: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4)
}

enum class CardPhase {
    Added,
    Learning,
    ReLearning,
    Review
}

/** FSRS-6 uses one stability (in days) and one difficulty. Null means never reviewed. */
data class MemoryState(val stability: Double, val difficulty: Double) {
    init {
        require(stability.isFinite() && stability >= 0.001) { "Invalid stability" }
        require(difficulty.isFinite() && difficulty in 1.0..10.0) { "Invalid difficulty" }
    }
}

/** All times are UTC epoch milliseconds; there is no fabricated last review for new cards. */
data class FlashCard(
    val id: String,
    val memory: MemoryState? = null,
    val dueAt: Long = 0,
    val lastReviewAt: Long? = null,
    val reviewCount: Int = 0,
    val lapseCount: Int = 0,
    val phase: CardPhase = CardPhase.Added,
    val learningStep: Int? = null,
    val interval: Int = 0
) {
    init {
        require(id.isNotBlank()) { "Card id must not be blank" }
        require(dueAt >= 0 && (lastReviewAt == null || lastReviewAt >= 0)) { "Invalid timestamp" }
        require(reviewCount >= 0 && lapseCount in 0..reviewCount && interval >= 0) { "Invalid counters" }
        if (phase == CardPhase.Added) {
            require(memory == null && lastReviewAt == null && reviewCount == 0 && interval == 0)
            require(learningStep == null)
        } else {
            require(memory != null && lastReviewAt != null && reviewCount > 0)
            require(dueAt >= lastReviewAt)
            if (phase == CardPhase.Review) {
                require(learningStep == null && interval > 0)
            } else {
                require(learningStep != null && learningStep >= 0 && interval == 0)
            }
        }
    }
}

data class ReviewLog(
    val cardId: String,
    val rating: Rating,
    val reviewedAt: Long,
    val elapsedDays: Long,
    val algorithmVersion: Int = FSRS6.ALGORITHM_VERSION
) {
    init {
        require(cardId.isNotBlank() && reviewedAt >= 0 && elapsedDays >= 0)
        require(algorithmVersion == FSRS6.ALGORITHM_VERSION) { "Unsupported FSRS version" }
    }
}

/** A complete proposal: persist this exact card and log after the user selects the rating. */
data class Grade internal constructor(
    val choice: Rating,
    val before: FlashCard,
    val card: FlashCard,
    val log: ReviewLog,
    val durationMillis: Long
) {
    val interval: Int get() = card.interval
}
