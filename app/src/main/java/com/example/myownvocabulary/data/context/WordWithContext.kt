package com.example.myownvocabulary.data.context

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myownvocabulary.data.word.WordEntity

data class WordWithContext(
    @Embedded val word: WordEntity,
    @Relation(parentColumn = "id", entityColumn = "wordId")
    val contexts: List<ContextSentenceEntity>
)
