package com.example.myownvocabulary.data.entry

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EntryWithTags(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(EntryTagCrossRef::class, parentColumn = "entryId", entityColumn = "tagId")
    )
    val tags: List<EntryTagEntity>
)
