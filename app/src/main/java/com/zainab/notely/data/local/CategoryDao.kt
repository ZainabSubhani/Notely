package com.zainab.notely.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.LiveData

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Query("UPDATE categories SET name = :name WHERE id = :id AND isDefault = 0")
    suspend fun renameCustom(id: Int, name: String): Int

    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomById(id: Int): Int

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, id ASC")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, id ASC")
    fun observeAll(): LiveData<List<Category>>

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByName(name: String): Int
}