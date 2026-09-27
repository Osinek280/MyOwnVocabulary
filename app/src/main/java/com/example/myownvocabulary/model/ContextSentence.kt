package com.example.myownvocabulary.model

import com.example.myownvocabulary.data.context.ContextSentenceEntity
import com.example.myownvocabulary.data.context.TextSpan
import java.util.UUID

data class ContextSentence(
    val id: String,
    val sentence: String,
    val translation: String,
    val highlights: List<TextSpan>
)

fun ContextSentenceEntity.toModel() = ContextSentence(
    id = id,
    sentence = sentence,
    translation = translation,
    highlights = highlights
)

fun ContextSentence.toEntity(wordId: String) = ContextSentenceEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    wordId = wordId,
    sentence = sentence.trim(),
    translation = translation.trim(),
    highlights = highlights
)
