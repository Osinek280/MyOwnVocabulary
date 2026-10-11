package com.example.myownvocabulary.data.learning

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningSessionDao {
    @Insert
    suspend fun insert(session: LearningSessionEntity)

    @Update
    suspend fun update(session: LearningSessionEntity)

    @Query("SELECT * FROM learning_sessions WHERE id = :id")
    suspend fun getById(id: String): LearningSessionEntity?

    @Query(
        """SELECT * FROM learning_sessions
        WHERE completedAt IS NOT NULL AND completedAt >= :fromTimestamp
        ORDER BY completedAt DESC"""
    )
    fun observeCompletedSince(fromTimestamp: Long): Flow<List<LearningSessionEntity>>

    @Query(
        """SELECT COUNT(*) FROM learning_sessions
        WHERE completedAt IS NOT NULL AND completedAt >= :fromTimestamp AND completedAt < :toTimestamp"""
    )
    fun observeCompletedCount(fromTimestamp: Long, toTimestamp: Long): Flow<Int>

    @Query("DELETE FROM learning_sessions WHERE completedAt IS NULL AND startedAt < :beforeTimestamp")
    suspend fun deleteAbandonedBefore(beforeTimestamp: Long)
}
