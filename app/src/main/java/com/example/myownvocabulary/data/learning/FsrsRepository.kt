package com.example.myownvocabulary.data.learning

import androidx.room.withTransaction
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.model.fsrs.FSRS6
import com.example.myownvocabulary.model.fsrs.FlashCard
import com.example.myownvocabulary.model.fsrs.Grade
import com.example.myownvocabulary.model.fsrs.ReviewLog

class FsrsRepository(private val database: AppDatabase, private val scheduler: FSRS6 = FSRS6()) {
    private val dao = database.fsrsDao()

    suspend fun preview(entryId: String, reviewedAt: Long = System.currentTimeMillis()): List<Grade> =
        database.withTransaction {
            require(dao.entryExists(entryId)) { "Vocabulary entry does not exist" }
            scheduler.calculate(dao.getCard(entryId)?.toCard() ?: FlashCard(entryId), reviewedAt)
        }

    /** Store exactly the selected preview, atomically with its history; reject stale/double submissions. */
    suspend fun save(proposal: Grade): FlashCard = database.withTransaction {
        require(dao.entryExists(proposal.before.id)) { "Vocabulary entry no longer exists" }
        val current = dao.getCard(proposal.before.id)?.toCard() ?: FlashCard(proposal.before.id)
        check(current == proposal.before) { "Card changed since preview; refresh the choices" }
        require(scheduler.review(current, proposal.choice, proposal.log.reviewedAt) == proposal) {
            "Proposal does not match this scheduler"
        }
        dao.upsertCard(FsrsCardEntity.fromCard(proposal.card))
        dao.insertReview(FsrsReviewEntity.fromLog(proposal.log))
        proposal.card
    }

    suspend fun history(entryId: String): List<ReviewLog> = dao.getHistory(entryId).map { it.toLog() }

    suspend fun due(now: Long = System.currentTimeMillis()): List<FlashCard> {
        require(now >= 0)
        return dao.getDue(now).map { it.toCard() }
    }
}
