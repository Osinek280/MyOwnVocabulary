package com.example.myownvocabulary.data

import androidx.room.TypeConverter
import com.example.myownvocabulary.data.context.TextSpan
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech

class Converters {
    @TypeConverter
    fun fromPartOfSpeech(value: PartOfSpeech?): String? = value?.name

    @TypeConverter
    fun toPartOfSpeech(value: String?): PartOfSpeech? = value?.let { PartOfSpeech.valueOf(it) }

    @TypeConverter
    fun fromSpans(spans: List<TextSpan>): String = spans.joinToString(",") { "${it.start}:${it.end}" }

    @TypeConverter
    fun toSpans(value: String): List<TextSpan> = if (value.isBlank()) {
        emptyList()
    } else {
        value.split(",").map { part ->
            val (start, end) = part.split(":")
            TextSpan(start.toInt(), end.toInt())
        }
    }

    @TypeConverter
    fun fromEntryKind(value: EntryKind): String = value.name

    @TypeConverter
    fun toEntryKind(value: String): EntryKind = EntryKind.valueOf(value)
}
