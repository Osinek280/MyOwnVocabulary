package com.example.myownvocabulary.data.transfer

import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech

enum class TransferScope {
    All,
    Filtered
}

data class TransferSelection(
    val scope: TransferScope = TransferScope.All,
    val languageCodes: Set<String> = emptySet(),
    val kinds: Set<EntryKind> = emptySet(),
    val tags: Set<String> = emptySet()
)

data class TransferPreview(
    val id: String,
    val term: String,
    val translation: String,
    val languageCode: String,
    val kind: EntryKind,
    val partOfSpeech: PartOfSpeech
)

fun TransferSelection.matches(languageCode: String, kind: EntryKind): Boolean {
    if (scope == TransferScope.All) return true
    val languageMatches = languageCodes.isEmpty() || languageCode in languageCodes
    val kindMatches = kinds.isEmpty() || kind in kinds
    return languageMatches && kindMatches
}
