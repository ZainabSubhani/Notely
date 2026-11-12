package com.zainab.notely.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChecklistItemDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var checklistDao: ChecklistDao
    private lateinit var itemDao: ChecklistItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        checklistDao = db.checklistDao()
        itemDao = db.checklistItemDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun deletingChecklistCascadesToItsItems() = runBlocking {
        val checklistId = checklistDao.insert(
            Checklist(title = "Groceries", createdAt = 1L, updatedAt = 1L)
        ).toInt()

        itemDao.insert(ChecklistItem(checklistId = checklistId, text = "Milk", position = 0))
        itemDao.insert(ChecklistItem(checklistId = checklistId, text = "Eggs", position = 1))

        assertEquals(2, itemDao.getForChecklist(checklistId).size)

        checklistDao.deleteById(checklistId)

        assertTrue(itemDao.getForChecklist(checklistId).isEmpty())
    }

    @Test
    fun nextPositionGrowsAndItemsCanBeChecked() = runBlocking {
        val id = checklistDao.insert(Checklist(title = "T", createdAt = 1L, updatedAt = 1L)).toInt()
        assertEquals(0, itemDao.nextPosition(id))
        itemDao.insert(ChecklistItem(checklistId = id, text = "A", position = itemDao.nextPosition(id)))
        assertEquals(1, itemDao.nextPosition(id))

        val item = itemDao.getForChecklist(id)[0]
        itemDao.setChecked(item.id, true)
        assertTrue(itemDao.getForChecklist(id)[0].isChecked)
    }
}