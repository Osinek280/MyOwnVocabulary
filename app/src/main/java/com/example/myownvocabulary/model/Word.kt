package com.example.myownvocabulary.model

import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.data.word.WordEntity

data class Word(
    val id: String,
    val term: String,
    val translation: String,
    val languageCode: String,
    val partOfSpeech: PartOfSpeech
)

fun WordEntity.toModel() = Word(
    id = id,
    term = term,
    translation = translation,
    languageCode = languageCode,
    partOfSpeech = partOfSpeech
)
