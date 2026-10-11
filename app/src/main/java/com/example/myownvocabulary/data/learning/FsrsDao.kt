package com.example.myownvocabulary.data.learning

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FsrsDao {
    @Query("SELECT * FROM fsrs_cards WHERE entryId = :entryId")
    suspend fun getCard(entryId: String): FsrsCardEntity?

    @Query("SELECT * FROM fsrs_cards WHERE dueAt <= :now ORDER BY dueAt, entryId")
    suspend fun getDue(now: Long): List<FsrsCardEntity>

    @Query("SELECT * FROM fsrs_reviews WHERE entryId = :entryId ORDER BY reviewedAt, id")
    suspend fun getHistory(entryId: String): List<FsrsReviewEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM entries WHERE id = :entryId)")
    suspend fun entryExists(entryId: String): Boolean

    @Upsert
    suspend fun upsertCard(card: FsrsCardEntity)

    @Insert
    suspend fun insertReview(review: FsrsReviewEntity)
}
