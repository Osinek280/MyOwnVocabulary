package com.example.myownvocabulary

import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.transfer.FileContext
import com.example.myownvocabulary.data.transfer.FileEntry
import com.example.myownvocabulary.data.transfer.FileSpan
import com.example.myownvocabulary.data.transfer.TransferScope
import com.example.myownvocabulary.data.transfer.TransferSelection
import com.example.myownvocabulary.data.transfer.VocabularyCodec
import com.example.myownvocabulary.data.transfer.VocabularyFormatException
import com.example.myownvocabulary.data.transfer.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyCodecTest {
    @Test
    fun roundTrip_keepsEntriesAndTags() {
        val entry = sampleEntry().copy(tags = listOf("school"))

        val decoded = VocabularyCodec.decode(VocabularyCodec.encode(listOf(entry)))

        assertEquals(0, decoded.skipped)
        assertEquals(1, decoded.entries.size)
        assertEquals(entry, decoded.entries.first())
    }

    @Test
    fun decode_rejectsUnsupportedVersion() {
        val raw = """{"version":2,"entries":[]}"""

        val error = assertThrows(VocabularyFormatException::class.java) {
            VocabularyCodec.decode(raw)
        }

        assertEquals("Nieobsługiwana wersja pliku.", error.message)
    }

    @Test
    fun decode_rejectsBrokenJson() {
        assertThrows(VocabularyFormatException::class.java) {
            VocabularyCodec.decode("{")
        }
    }

    @Test
    fun decode_skipsUnknownEnumAndBlankTerm() {
        val raw = """
            {
              "version": 1,
              "entries": [
                {
                  "id": "bad-kind",
                  "term": "apple",
                  "translation": "jabłko",
                  "languageCode": "en",
                  "partOfSpeech": "Noun",
                  "kind": "Nope",
                  "createdAt": 1
                },
                {
                  "id": "blank",
                  "term": "   ",
                  "translation": "x",
                  "languageCode": "en",
                  "partOfSpeech": "Noun",
                  "kind": "Word",
                  "createdAt": 2
                },
                {
                  "id": "ok",
                  "term": " apple ",
                  "translation": " jabłko ",
                  "languageCode": "en",
                  "partOfSpeech": "Noun",
                  "kind": "Word",
                  "createdAt": 3,
                  "contexts": [
                    {
                      "id": "",
                      "sentence": "An apple",
                      "translation": "Jabłko",
                      "highlights": [{ "start": 3, "end": 8 }]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val decoded = VocabularyCodec.decode(raw)

        assertEquals(2, decoded.skipped)
        assertEquals(1, decoded.entries.size)
        val entry = decoded.entries.first()
        assertEquals("ok", entry.id)
        assertEquals("apple", entry.term)
        assertEquals("jabłko", entry.translation)
        assertEquals(emptyList<String>(), entry.tags)
        assertTrue(entry.contexts.single().id.isNotBlank())
        assertEquals(FileSpan(3, 8), entry.contexts.single().highlights.single())
    }

    @Test
    fun decode_skipsBlankTranslation() {
        for (translation in listOf("", " \t\n")) {
            val invalid = sampleEntry().copy(translation = translation)
            val valid = sampleEntry().copy(id = "valid")

            val decoded = VocabularyCodec.decode(VocabularyCodec.encode(listOf(invalid, valid)))

            assertEquals(1, decoded.skipped)
            assertEquals(listOf(valid), decoded.entries)
        }
    }

    @Test
    fun decode_assignsIdWhenMissingAndKeepsTags() {
        val raw = """
            {
              "version": 1,
              "entries": [
                {
                  "id": "",
                  "term": "house",
                  "translation": "dom",
                  "languageCode": "en",
                  "partOfSpeech": "Noun",
                  "kind": "Word",
                  "createdAt": 4,
                  "tags": ["school"]
                }
              ]
            }
        """.trimIndent()

        val decoded = VocabularyCodec.decode(raw)
        val entry = decoded.entries.single()

        assertEquals(0, decoded.skipped)
        assertTrue(entry.id.isNotBlank())
        assertEquals(listOf("school"), entry.tags)
    }

    @Test
    fun selection_filtersByLanguageAndKind_andIgnoresTags() {
        val selection = TransferSelection(
            scope = TransferScope.Filtered,
            languageCodes = setOf("en"),
            kinds = setOf(EntryKind.Idiom),
            tags = setOf("school")
        )

        assertTrue(selection.matches("en", EntryKind.Idiom))
        assertFalse(selection.matches("de", EntryKind.Idiom))
        assertFalse(selection.matches("en", EntryKind.Word))
    }

    @Test
    fun selection_tagsAloneDoNotNarrow() {
        val selection = TransferSelection(
            scope = TransferScope.Filtered,
            tags = setOf("school")
        )

        assertTrue(selection.matches("pl", EntryKind.Sentence))
    }

    @Test
    fun selection_emptyDimensionMeansAllOfThatDimension() {
        val selection = TransferSelection(
            scope = TransferScope.Filtered,
            languageCodes = setOf("de")
        )

        assertTrue(selection.matches("de", EntryKind.Word))
        assertTrue(selection.matches("de", EntryKind.Sentence))
        assertFalse(selection.matches("en", EntryKind.Word))
    }

    private fun sampleEntry() = FileEntry(
        id = "1",
        term = "apple",
        translation = "jabłko",
        languageCode = "en",
        partOfSpeech = "Noun",
        kind = "Word",
        createdAt = 10,
        tags = emptyList(),
        contexts = listOf(
            FileContext(
                id = "c1",
                sentence = "An apple",
                translation = "Jabłko",
                highlights = listOf(FileSpan(3, 8))
            )
        )
    )
}
