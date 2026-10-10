package com.example.myownvocabulary

import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.prefs.HomeSort
import com.example.myownvocabulary.data.prefs.sortEntries
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.model.toModel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSortTest {
    private fun entry(id: String, term: String, date: Long) =
        Entry(id, term, "", "pl", EntryKind.Word, createdAt = date)

    @Test fun chronologicalSortUsesDatesWithStableTies() {
        val entries = listOf(entry("b", "B", 10), entry("c", "C", 20), entry("a", "A", 10))
        assertEquals(listOf("c", "a", "b"), HomeSort.Newest.sortEntries(entries).map { it.id })
        assertEquals(listOf("a", "b", "c"), HomeSort.Oldest.sortEntries(entries).map { it.id })
    }

    @Test fun alphabeticalSortRespectsPolishLettersAndIgnoresCase() {
        val entries =
            listOf(entry("z", "Żaba", 0), entry("b", "banan", 0), entry("a", "Ącki", 0), entry("c", "Ananas", 0))
        assertEquals(listOf("c", "a", "b", "z"), HomeSort.TermAscending.sortEntries(entries).map { it.id })
        assertEquals(listOf("z", "b", "a", "c"), HomeSort.TermDescending.sortEntries(entries).map { it.id })
    }

    @Test fun entityMappingPreservesCreationDate() {
        val entity =
            EntryEntity(
                term = "test",
                translation = "test",
                languageCode = "en",
                createdAt = 123L,
                kind = EntryKind.Word
            )
        assertEquals(123L, entity.toModel().createdAt)
    }
}
