package com.zainab.notely.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zainab.notely.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChecklistProgressTest {

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
    fun progressCountsCheckedAndTotalCorrectly() = runBlocking {
        val id = checklistDao.insert(
            Checklist(title = "Test", createdAt = 1L, updatedAt = 1L)
        ).toInt()
        itemDao.insert(ChecklistItem(checklistId = id, text = "A", isChecked = true, position = 0))
        itemDao.insert(ChecklistItem(checklistId = id, text = "B", isChecked = false, position = 1))
        itemDao.insert(ChecklistItem(checklistId = id, text = "C", isChecked = true, position = 2))

       val result = checklistDao.getFilteredWithProgress(null).getOrAwaitValue()

        assertEquals(1, result.size)
        assertEquals(3, result[0].totalItems)
        assertEquals(2, result[0].doneItems)
    }

    @Test
    fun checklistWithNoItemsShowsZeroOfZero() = runBlocking {
        checklistDao.insert(Checklist(title = "Empty", createdAt = 1L, updatedAt = 1L))

        val result = checklistDao.getFilteredWithProgress(null).getOrAwaitValue()

        assertEquals(1, result.size)
        assertEquals(0, result[0].totalItems)
        assertEquals(0, result[0].doneItems)
    }

    @Test
    fun filterAndCategoryFieldsWork() = runBlocking {
        val catId = db.categoryDao().insert(Category(name = "A", colorHex = "#2DD4BF")).toInt()
        checklistDao.insert(Checklist(title = "In", categoryId = catId, createdAt = 1L, updatedAt = 1L))
        checklistDao.insert(Checklist(title = "Out", createdAt = 1L, updatedAt = 1L))

        val filtered = checklistDao.getFilteredWithProgress(catId).getOrAwaitValue()
        assertEquals(1, filtered.size)
        assertEquals("A", filtered[0].categoryName)
        assertEquals(2, checklistDao.getFilteredWithProgress(null).getOrAwaitValue().size)
    }
}