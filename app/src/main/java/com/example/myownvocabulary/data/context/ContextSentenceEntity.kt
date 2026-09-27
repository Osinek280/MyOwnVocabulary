package com.example.myownvocabulary.data.context

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myownvocabulary.data.entry.EntryEntity
import java.util.UUID

@Entity(
    tableName = "context_sentences",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entryId"])]
)
data class ContextSentenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entryId: String,
    val sentence: String,
    val translation: String,
    val highlights: List<TextSpan>
)

data class TextSpan(val start: Int, val end: Int)
