package com.example.myownvocabulary.data.word

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val term: String,
    val translation: String,
    val languageCode: String,
    val createdAt: Long,
    val partOfSpeech: PartOfSpeech
)
