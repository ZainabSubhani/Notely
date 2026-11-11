package com.zainab.notely.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Int): Note?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAll(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeById(id: Int): LiveData<Note?>

    @Query("""
    SELECT n.*, c.name AS categoryName, c.colorHex AS categoryColor
    FROM notes n
    LEFT JOIN categories c ON c.id = n.categoryId
    ORDER BY n.updatedAt DESC
""")
    fun getAllWithCategory(): LiveData<List<NoteWithCategory>>
}