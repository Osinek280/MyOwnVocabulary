package com.example.myownvocabulary.data.prefs

import com.example.myownvocabulary.model.Entry
import java.text.Collator
import java.util.Locale

enum class HomeSort(val label: String) {
    Newest("Najnowsze"),
    Oldest("Najstarsze"),
    TermAscending("Wyrażenie A–Z"),
    TermDescending("Wyrażenie Z–A")
}

data class HomeOptions(
    val languages: Set<String> = emptySet(),
    val kinds: Set<String> = emptySet(),
    val sort: HomeSort = HomeSort.Newest
)

fun HomeSort.sortEntries(entries: List<Entry>): List<Entry> {
    val collator = Collator.getInstance(Locale.forLanguageTag("pl"))
        .apply { strength = Collator.SECONDARY }
    val comparator = when (this) {
        HomeSort.Newest -> compareByDescending<Entry> { it.createdAt }
        HomeSort.Oldest -> compareBy<Entry> { it.createdAt }
        HomeSort.TermAscending -> Comparator<Entry> { a, b -> collator.compare(a.term, b.term) }
        HomeSort.TermDescending -> Comparator<Entry> { a, b -> collator.compare(b.term, a.term) }
    }
    return entries.sortedWith(comparator.thenBy { it.id })
}
