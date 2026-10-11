package com.example.myownvocabulary.data.learning

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningProgressDao {
    @Query("SELECT * FROM learning_progress")
    fun observeAll(): Flow<List<LearningProgressEntity>>

    @Query("SELECT * FROM learning_progress WHERE entryId = :entryId")
    fun observeByEntryId(entryId: String): Flow<LearningProgressEntity?>

    @Query(
        """SELECT * FROM learning_progress
        WHERE nextReviewAt IS NOT NULL AND nextReviewAt <= :timestamp
        ORDER BY nextReviewAt ASC"""
    )
    suspend fun getDue(timestamp: Long): List<LearningProgressEntity>

    @Upsert
    suspend fun upsert(progress: LearningProgressEntity)

    @Query("DELETE FROM learning_progress WHERE entryId = :entryId")
    suspend fun deleteByEntryId(entryId: String)
}
