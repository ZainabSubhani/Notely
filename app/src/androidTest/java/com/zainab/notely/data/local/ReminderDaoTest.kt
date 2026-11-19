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
class ReminderDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.reminderDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndReadBack() = runBlocking {
        val id = dao.insert(
            Reminder(title = "Submit listing", dueAt = 1000L, createdAt = 1L)
        ).toInt()
        val loaded = dao.getById(id)
        assertEquals("Submit listing", loaded?.title)
        assertEquals(false, loaded?.isCompleted)
    }

    @Test
    fun getAllOrdersBySoonestDueFirst() = runBlocking {
        dao.insert(Reminder(title = "Later", dueAt = 3000L, createdAt = 1L))
        dao.insert(Reminder(title = "Soonest", dueAt = 1000L, createdAt = 1L))
        dao.insert(Reminder(title = "Middle", dueAt = 2000L, createdAt = 1L))

        val all = dao.getAll()
        assertEquals(listOf("Soonest", "Middle", "Later"), all.map { it.title })
    }

    @Test
    fun setCompletedPersistsAndObserveAllIsOrdered() = runBlocking {
        val a = dao.insert(Reminder(title = "A", dueAt = 2000L, createdAt = 1L)).toInt()
        dao.insert(Reminder(title = "B", dueAt = 1000L, createdAt = 1L))

        dao.setCompleted(a, true)

        val all = dao.observeAll().getOrAwaitValue()
        assertEquals(listOf("B", "A"), all.map { it.title })
        assertEquals(true, all[1].isCompleted)
    }
}