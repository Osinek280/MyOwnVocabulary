package com.example.myownvocabulary.data.context

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myownvocabulary.data.word.WordEntity
import java.util.UUID

@Entity(
    tableName = "context_sentences",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"])]
)
data class ContextSentenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val wordId: String,
    val sentence: String,
    val translation: String,
    val highlights: List<TextSpan>
)

data class TextSpan(val start: Int, val end: Int)
