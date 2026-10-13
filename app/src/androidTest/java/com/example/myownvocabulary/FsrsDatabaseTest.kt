package com.example.myownvocabulary

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.learning.FsrsRepository
import com.example.myownvocabulary.model.fsrs.CardPhase
import com.example.myownvocabulary.model.fsrs.FSRS6
import com.example.myownvocabulary.model.fsrs.Rating
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FsrsDatabaseTest {
    private val start = 1_767_225_600_000L
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun entry(id: String) = EntryEntity(
        id = id,
        term = "cat",
        translation = "kot",
        languageCode = "en",
        createdAt = start,
        kind = EntryKind.Word
    )

    @Test
    fun persistsExactPreviewWithHistoryAndRejectsDuplicateOrStaleSave() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            database.entryDao().saveWithTags(entry("entry"), emptyList(), isNew = true)
            val repository = FsrsRepository(database)
            val choices = repository.preview("entry", start)
            val selected = choices.single { it.choice == Rating.Good }
            assertEquals(selected.card, repository.save(selected))
            assertEquals(selected.card, database.fsrsDao().getCard("entry")?.toCard())
            assertEquals(listOf(selected.log), repository.history("entry"))
            assertTrue(repository.due(start).isEmpty())
            assertEquals(listOf(selected.card), repository.due(selected.card.dueAt))

            for (stale in listOf(selected, choices.single { it.choice == Rating.Easy })) {
                try {
                    repository.save(stale)
                    fail("Stale proposal accepted")
                } catch (_: IllegalStateException) {
                    // Neither state nor history may change after a rejected submission.
                }
            }
            assertEquals(listOf(selected.log), repository.history("entry"))
            assertEquals(selected.card, database.fsrsDao().getCard("entry")?.toCard())
        } finally {
            database.close()
        }
    }

    @Test
    fun mismatchedSchedulerAndDeletedEntryCannotSavePreview() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            database.entryDao().saveWithTags(entry("entry"), emptyList(), isNew = true)
            val repository = FsrsRepository(database)
            val selected = repository.preview("entry", start).single { it.choice == Rating.Easy }
            val other = FsrsRepository(database, FSRS6(requestRetention = 0.99, enableFuzz = false))
            try {
                other.save(selected)
                fail("Mismatched scheduler accepted")
            } catch (_: IllegalArgumentException) {
                assertEquals(null, database.fsrsDao().getCard("entry"))
                assertTrue(repository.history("entry").isEmpty())
            }
            repository.save(selected)
            database.entryDao().deleteByIds(listOf("entry"))
            assertEquals(null, database.fsrsDao().getCard("entry"))
            assertTrue(repository.history("entry").isEmpty())
            try {
                repository.save(selected)
                fail("Deleted entry accepted")
            } catch (_: IllegalArgumentException) {
                assertTrue(repository.history("entry").isEmpty())
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun historyKeepsOrderEvenWhenReviewTimestampsMatch() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            database.entryDao().saveWithTags(entry("entry"), emptyList(), isNew = true)
            val repository = FsrsRepository(database)
            repository.save(repository.preview("entry", start).single { it.choice == Rating.Good })
            val second = repository.preview("entry", start).single { it.choice == Rating.Good }
            repository.save(second)
            val history = repository.history("entry")
            assertEquals(2, history.size)
            assertEquals(CardPhase.Review, second.card.phase)
            assertEquals(second.card, FSRS6().replay("entry", history))
        } finally {
            database.close()
        }
    }

    @Test
    fun migrationFromVersion11KeepsEntriesAndDropsObsoleteLearningTables() = runBlocking {
        val name = "fsrs-migration-${UUID.randomUUID()}.db"
        val builder = { Room.databaseBuilder(context, AppDatabase::class.java, name) }
        var database = builder().build()
        try {
            database.entryDao().saveWithTags(entry("entry"), emptyList(), isNew = true)
            val sqlite = database.openHelper.writableDatabase
            sqlite.execSQL("DROP TABLE fsrs_reviews")
            sqlite.execSQL("DROP TABLE fsrs_cards")
            sqlite.execSQL(
                """CREATE TABLE learning_progress (
                    entryId TEXT NOT NULL PRIMARY KEY,
                    status TEXT NOT NULL,
                    stage INTEGER NOT NULL,
                    correctStreak INTEGER NOT NULL,
                    totalCorrect INTEGER NOT NULL,
                    totalIncorrect INTEGER NOT NULL,
                    lastReviewedAt INTEGER,
                    nextReviewAt INTEGER
                )"""
            )
            sqlite.execSQL(
                """INSERT INTO learning_progress
                    (entryId, status, stage, correctStreak, totalCorrect, totalIncorrect)
                    VALUES ('entry', 'LEARNING', 0, 3, 7, 0)"""
            )
            sqlite.execSQL(
                """CREATE TABLE learning_sessions (
                    id TEXT NOT NULL PRIMARY KEY,
                    type TEXT NOT NULL,
                    startedAt INTEGER NOT NULL,
                    completedAt INTEGER,
                    plannedCount INTEGER NOT NULL,
                    answeredCount INTEGER NOT NULL,
                    correctCount INTEGER NOT NULL
                )"""
            )
            sqlite.version = 11
            database.close()

            database = builder()
                .addMigrations(AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13)
                .build()
            assertEquals(listOf(entry("entry")), database.entryDao().observeAllWithTags().first().map { it.entry })
            database.openHelper.readableDatabase.query(
                """SELECT name FROM sqlite_master
                WHERE type = 'table' AND name IN ('learning_progress', 'learning_sessions')"""
            ).use { cursor ->
                assertEquals(0, cursor.count)
            }
            assertTrue(database.fsrsDao().getDue(Long.MAX_VALUE).isEmpty())
            val repository = FsrsRepository(database)
            repository.save(repository.preview("entry", start).single { it.choice == Rating.Good })
            assertEquals(1, repository.history("entry").size)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
