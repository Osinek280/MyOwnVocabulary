package com.example.myownvocabulary.data.entry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Transaction
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeAllWithTags(): Flow<List<EntryWithTags>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<EntryTagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagLinks(links: List<EntryTagCrossRef>)

    @Query("DELETE FROM entry_tags WHERE entryId = :entryId")
    suspend fun clearTagLinks(entryId: String)

    @Transaction
    suspend fun saveWithTags(entry: EntryEntity, tags: List<EntryTagEntity>, isNew: Boolean) {
        if (isNew) insert(entry) else update(entry)
        clearTagLinks(entry.id)
        if (tags.isNotEmpty()) {
            insertTags(tags)
            insertTagLinks(tags.map { EntryTagCrossRef(entry.id, it.id) })
        }
    }

    @Transaction
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
