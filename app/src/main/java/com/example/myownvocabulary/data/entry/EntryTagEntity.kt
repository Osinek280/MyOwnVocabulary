package com.example.myownvocabulary.data.entry

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tags")
data class EntryTagEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val hue: Float
)
