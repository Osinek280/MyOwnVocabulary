package com.example.myownvocabulary.data

import androidx.room.TypeConverter
import com.example.myownvocabulary.data.word.PartOfSpeech

class Converters {
    @TypeConverter
    fun fromPartOfSpeech(value: PartOfSpeech): String = value.name

    @TypeConverter
    fun toPartOfSpeech(value: String): PartOfSpeech = PartOfSpeech.valueOf(value)
}
