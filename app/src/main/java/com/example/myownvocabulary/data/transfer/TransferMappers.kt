package com.example.myownvocabulary.data.transfer

import com.example.myownvocabulary.data.context.ContextSentenceEntity
import com.example.myownvocabulary.data.context.TextSpan
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech
import java.util.UUID

internal fun EntryEntity.toFileEntry(contexts: List<ContextSentenceEntity>) = FileEntry(
    id = id,
    term = term,
    translation = translation,
    languageCode = languageCode,
    partOfSpeech = partOfSpeech?.name,
    kind = kind.name,
    meaning = meaning,
    numericValue = numericValue,
    createdAt = createdAt,
    tags = emptyList(),
    contexts = contexts.map { it.toFileContext() }
)

internal fun FileEntry.toEntity() = EntryEntity(
    id = id,
    term = term.trim(),
    translation = translation.trim(),
    languageCode = languageCode,
    createdAt = createdAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
    partOfSpeech = partOfSpeech?.let { PartOfSpeech.valueOf(it) },
    kind = EntryKind.valueOf(kind),
    meaning = meaning?.trim()?.takeIf { it.isNotEmpty() },
    numericValue = numericValue?.trim()?.takeIf { it.isNotEmpty() }
)

internal fun FileContext.toEntity(entryId: String) = ContextSentenceEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    entryId = entryId,
    sentence = sentence.trim(),
    translation = translation.trim(),
    highlights = highlights.map { TextSpan(it.start, it.end) }
)

private fun ContextSentenceEntity.toFileContext() = FileContext(
    id = id,
    sentence = sentence,
    translation = translation,
    highlights = highlights.map { FileSpan(it.start, it.end) }
)
