package com.example.myownvocabulary.data.entry

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val term: String,
    val translation: String,
    val languageCode: String,
    val createdAt: Long,
    val partOfSpeech: PartOfSpeech
)
