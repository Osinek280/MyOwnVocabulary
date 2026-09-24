package com.example.myownvocabulary.model

import com.example.myownvocabulary.data.context.TextSpan
import com.example.myownvocabulary.data.context.WordWithContext
import com.example.myownvocabulary.data.word.PartOfSpeech

data class ContextSentence(
    val id: String,
    val sentence: String,
    val translation: String,
    val highlights: List<TextSpan>
)

data class Word(
    val id: String,
    val term: String,
    val translation: String,
    val languageCode: String,
    val partOfSpeech: PartOfSpeech,
    val contexts: List<ContextSentence>
)

fun WordWithContext.toWord() = Word(
    id = word.id,
    term = word.term,
    translation = word.translation,
    languageCode = word.languageCode,
    partOfSpeech = word.partOfSpeech,
    contexts = contexts.map {
        ContextSentence(it.id, it.sentence, it.translation, it.highlights)
    }
)
