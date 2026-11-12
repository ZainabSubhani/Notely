package com.zainab.notely.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChecklistDao {
    @Insert
    suspend fun insert(checklist: Checklist): Long

    @Update
    suspend fun update(checklist: Checklist)

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getById(id: Int): Checklist?

    @Query("""
    SELECT c.id AS id, c.title AS title, c.categoryId AS categoryId,
           cat.name AS categoryName, cat.colorHex AS categoryColor,
           COUNT(ci.id) AS totalItems,
           COALESCE(SUM(ci.isChecked), 0) AS doneItems
    FROM checklists c
    LEFT JOIN categories cat ON cat.id = c.categoryId
    LEFT JOIN checklist_items ci ON ci.checklistId = c.id
    WHERE (:categoryId IS NULL OR c.categoryId = :categoryId)
    GROUP BY c.id
    ORDER BY c.updatedAt DESC
""")
    fun getFilteredWithProgress(categoryId: Int?): LiveData<List<ChecklistWithProgress>>

    @Query("SELECT COUNT(*) FROM checklists")
    fun observeCount(): LiveData<Int>

    @Query("SELECT * FROM checklists WHERE id = :id")
    fun observeById(id: Int): LiveData<Checklist?>
}