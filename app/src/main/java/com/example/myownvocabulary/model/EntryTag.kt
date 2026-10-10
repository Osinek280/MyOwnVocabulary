package com.example.myownvocabulary.model

import com.example.myownvocabulary.data.entry.EntryTagEntity
import java.util.UUID

data class EntryTag(val id: String = UUID.randomUUID().toString(), val label: String, val hue: Float)

fun EntryTagEntity.toModel() = EntryTag(id = id, label = label, hue = hue)

fun EntryTag.toEntity() = EntryTagEntity(id = id, label = label, hue = hue)
