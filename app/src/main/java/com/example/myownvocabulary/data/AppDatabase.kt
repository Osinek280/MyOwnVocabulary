package com.example.myownvocabulary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myownvocabulary.data.context.ContextSentenceDao
import com.example.myownvocabulary.data.context.ContextSentenceEntity
import com.example.myownvocabulary.data.entry.EntryDao
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryTagCrossRef
import com.example.myownvocabulary.data.entry.EntryTagEntity

@Database(
    entities = [EntryEntity::class, ContextSentenceEntity::class, EntryTagEntity::class, EntryTagCrossRef::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun contextSentenceDao(): ContextSentenceDao

    companion object {
        internal val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tags (id TEXT NOT NULL PRIMARY KEY, label TEXT NOT NULL, hue REAL NOT NULL)"
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS entry_tags (
                    entryId TEXT NOT NULL,
                    tagId TEXT NOT NULL,
                    PRIMARY KEY(entryId, tagId),
                    FOREIGN KEY(entryId) REFERENCES entries(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(tagId) REFERENCES tags(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_entry_tags_tagId ON entry_tags(tagId)")
            }
        }
        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN meaning TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE entries ADD COLUMN numericValue TEXT NOT NULL DEFAULT ''")
            }
        }

        internal val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Preserve child rows before replacing their parent table (ON DELETE CASCADE).
                db.execSQL("CREATE TEMP TABLE context_backup AS SELECT * FROM context_sentences")
                db.execSQL(
                    """CREATE TABLE entries_nullable (
                    id TEXT NOT NULL PRIMARY KEY,
                    term TEXT NOT NULL,
                    translation TEXT NOT NULL,
                    languageCode TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    partOfSpeech TEXT,
                    kind TEXT NOT NULL,
                    meaning TEXT,
                    numericValue TEXT
                )"""
                )
                db.execSQL(
                    """INSERT INTO entries_nullable
                    SELECT id, term, translation, languageCode, createdAt,
                        CASE WHEN kind = 'Word' THEN partOfSpeech ELSE NULL END,
                        kind,
                        CASE WHEN kind = 'Idiom' THEN NULLIF(TRIM(meaning), '') ELSE NULL END,
                        CASE WHEN kind = 'Numeral' THEN NULLIF(TRIM(numericValue), '') ELSE NULL END
                    FROM entries"""
                )
                db.execSQL("DROP TABLE entries")
                db.execSQL("ALTER TABLE entries_nullable RENAME TO entries")
                db.execSQL("INSERT OR REPLACE INTO context_sentences SELECT * FROM context_backup")
                db.execSQL("DROP TABLE context_backup")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vocabulary.db"
            )
                .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}
