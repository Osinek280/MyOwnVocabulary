package com.example.myownvocabulary.data.context

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContextSentenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sentences: List<ContextSentenceEntity>)

    @Query("DELETE FROM context_sentences WHERE id = :id")
    suspend fun deleteById(id: String)
}
