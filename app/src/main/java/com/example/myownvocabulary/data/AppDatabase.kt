package com.example.myownvocabulary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myownvocabulary.data.context.ContextSentenceDao
import com.example.myownvocabulary.data.context.ContextSentenceEntity
import com.example.myownvocabulary.data.word.WordDao
import com.example.myownvocabulary.data.word.WordEntity

@Database(
    entities = [WordEntity::class, ContextSentenceEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun contextSentenceDao(): ContextSentenceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vocabulary.db"
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}
