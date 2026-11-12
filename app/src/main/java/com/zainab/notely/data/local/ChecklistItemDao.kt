package com.zainab.notely.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.LiveData

@Dao
interface ChecklistItemDao {
    @Insert
    suspend fun insert(item: ChecklistItem): Long

    @Update
    suspend fun update(item: ChecklistItem)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY position ASC")
    suspend fun getForChecklist(checklistId: Int): List<ChecklistItem>

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY position ASC, id ASC")
    fun observeForChecklist(checklistId: Int): LiveData<List<ChecklistItem>>

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Int, checked: Boolean)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun nextPosition(checklistId: Int): Int
}