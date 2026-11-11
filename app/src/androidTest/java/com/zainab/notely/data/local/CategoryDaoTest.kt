package com.zainab.notely.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.zainab.notely.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.categoryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndReadBack() = runBlocking {
        dao.insert(Category(name = "Work", colorHex = "#6366F1", isDefault = true))
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("Work", all[0].name)
    }

    @Test
    fun defaultCategoryCannotBeDeleted() = runBlocking {
        val id = dao.insert(Category(name = "Work", colorHex = "#6366F1", isDefault = true)).toInt()
        assertEquals(0, dao.deleteCustomById(id))
        assertEquals(1, dao.getAll().size)
    }

    @Test
    fun customCategoryCanBeDeleted() = runBlocking {
        val id = dao.insert(Category(name = "Teal one", colorHex = "#2DD4BF")).toInt()
        assertEquals(1, dao.deleteCustomById(id))
        assertEquals(0, dao.getAll().size)
    }
    @Test
    fun defaultCategoryCannotBeRenamed() = runBlocking {
        val id = dao.insert(Category(name = "Work", colorHex = "#6366F1", isDefault = true)).toInt()
        assertEquals(0, dao.renameCustom(id, "Hacked"))
        assertEquals("Work", dao.getAll()[0].name)
    }

    @Test
    fun customCategoryCanBeRenamed() = runBlocking {
        val id = dao.insert(Category(name = "Teal one", colorHex = "#2DD4BF")).toInt()
        assertEquals(1, dao.renameCustom(id, "Fitness"))
        assertEquals("Fitness", dao.getAll()[0].name)
    }

    @Test
    fun observeAllListsDefaultsBeforeCustom() = runBlocking {
        dao.insert(Category(name = "Teal one", colorHex = "#2DD4BF"))
        dao.insert(Category(name = "Work", colorHex = "#6366F1", isDefault = true))
        val names = dao.observeAll().getOrAwaitValue().map { it.name }
        assertEquals(listOf("Work", "Teal one"), names)
    }

    @Test
    fun nameCheckIgnoresCase() = runBlocking {
        dao.insert(Category(name = "Work", colorHex = "#6366F1", isDefault = true))
        assertEquals(1, dao.countByName("work"))
        assertEquals(0, dao.countByName("Fitness"))
    }
}