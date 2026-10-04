package com.example.myownvocabulary.data.transfer

import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech
import kotlinx.serialization.Serializable

@Serializable
data class VocabularyFile(val version: Int, val entries: List<FileEntry> = emptyList())

@Serializable
data class FileEntry(
    val id: String = "",
    val term: String,
    val translation: String,
    val languageCode: String,
    val partOfSpeech: String,
    val kind: String,
    val createdAt: Long = 0,
    val tags: List<String> = emptyList(),
    val contexts: List<FileContext> = emptyList()
)

@Serializable
data class FileContext(
    val id: String = "",
    val sentence: String,
    val translation: String = "",
    val highlights: List<FileSpan> = emptyList()
)

@Serializable
data class FileSpan(val start: Int, val end: Int)

data class DecodedVocabulary(val entries: List<FileEntry>, val skipped: Int)

fun FileEntry.toPreview(): TransferPreview? {
    val parsedKind = runCatching { EntryKind.valueOf(kind) }.getOrNull() ?: return null
    val parsedPartOfSpeech = runCatching { PartOfSpeech.valueOf(partOfSpeech) }.getOrNull() ?: return null
    return TransferPreview(
        id = id,
        term = term,
        translation = translation,
        languageCode = languageCode,
        kind = parsedKind,
        partOfSpeech = parsedPartOfSpeech
    )
}
