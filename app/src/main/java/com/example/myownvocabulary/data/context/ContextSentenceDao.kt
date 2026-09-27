package com.example.myownvocabulary.data.context

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextSentenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sentences: List<ContextSentenceEntity>)

    @Query("DELETE FROM context_sentences WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM context_sentences WHERE wordId = :wordId")
    fun observeByWordId(wordId: String): Flow<List<ContextSentenceEntity>>

    @Query("SELECT * FROM context_sentences WHERE wordId = :wordId")
    suspend fun getByWordId(wordId: String): List<ContextSentenceEntity>
}
