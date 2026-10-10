package com.example.myownvocabulary

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.EntryTagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class EntryTagsDatabaseTest {
    @Test
    fun sharedTagSurvivesUnlinkingAndDeletingAnotherEntry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = database.entryDao()
            val first =
                EntryEntity(
                    term = "cat",
                    translation = "kot",
                    languageCode = "en",
                    createdAt = 1,
                    kind = EntryKind.Word
                )
            val second = first.copy(id = "second", term = "dog", translation = "pies")
            val tag = EntryTagEntity(label = "animals", hue = 235f)
            dao.saveWithTags(first, listOf(tag), isNew = true)
            dao.saveWithTags(second, listOf(tag), isNew = true)
            assertEquals(listOf(tag), dao.observeAllWithTags().first().first { it.entry.id == first.id }.tags)

            dao.saveWithTags(first.copy(term = "kitten"), emptyList(), isNew = false)
            val entries = dao.observeAllWithTags().first()
            assertEquals(emptyList<EntryTagEntity>(), entries.first { it.entry.id == first.id }.tags)
            assertEquals(listOf(tag), entries.first { it.entry.id == second.id }.tags)

            dao.deleteByIds(listOf(first.id))
            assertEquals(listOf(tag), dao.observeAllWithTags().first().single().tags)
        } finally {
            database.close()
        }
    }
}
