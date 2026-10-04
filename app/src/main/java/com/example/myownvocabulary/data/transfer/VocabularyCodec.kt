package com.example.myownvocabulary.data.transfer

import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech
import java.util.UUID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class VocabularyFormatException(message: String) : Exception(message)

object VocabularyCodec {
    const val VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(entries: List<FileEntry>): String {
        val file = VocabularyFile(
            version = VERSION,
            entries = entries
//            entries = entries.map { entry -> entry.copy(tags = emptyList()) }
        )
        return json.encodeToString(file)
    }

    fun decode(text: String): DecodedVocabulary {
        val parsed = try {
            json.decodeFromString<VocabularyFile>(text)
        } catch (error: SerializationException) {
            throw VocabularyFormatException("Nie udało się odczytać pliku.")
        } catch (error: IllegalArgumentException) {
            throw VocabularyFormatException("Nie udało się odczytać pliku.")
        }
        if (parsed.version != VERSION) {
            throw VocabularyFormatException("Nieobsługiwana wersja pliku.")
        }

        var skipped = 0
        val seenIds = mutableSetOf<String>()
        val entries = parsed.entries.mapNotNull { entry ->
            val accepted = accept(entry, seenIds)
            if (accepted == null) skipped++
            accepted
        }
        return DecodedVocabulary(entries = entries, skipped = skipped)
    }

    private fun accept(entry: FileEntry, seenIds: MutableSet<String>): FileEntry? {
        val kind = runCatching { EntryKind.valueOf(entry.kind) }.getOrNull()
        val partOfSpeech = runCatching { PartOfSpeech.valueOf(entry.partOfSpeech) }.getOrNull()
        if (kind == null || partOfSpeech == null || entry.term.isBlank()) return null

        val id = entry.id.ifBlank { UUID.randomUUID().toString() }
        if (!seenIds.add(id)) return null

        return entry.copy(
            id = id,
            term = entry.term.trim(),
            translation = entry.translation.trim(),
            kind = kind.name,
            partOfSpeech = partOfSpeech.name,
            tags = entry.tags,
            contexts = entry.contexts.map { context ->
                context.copy(id = context.id.ifBlank { UUID.randomUUID().toString() })
            }
        )
    }
}
