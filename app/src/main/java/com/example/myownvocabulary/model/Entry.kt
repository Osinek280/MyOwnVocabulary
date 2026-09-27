package com.example.myownvocabulary.model

import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.PartOfSpeech

data class Entry(
    val id: String,
    val term: String,
    val translation: String,
    val languageCode: String,
    val partOfSpeech: PartOfSpeech
)

fun EntryEntity.toModel() = Entry(
    id = id,
    term = term,
    translation = translation,
    languageCode = languageCode,
    partOfSpeech = partOfSpeech
)
