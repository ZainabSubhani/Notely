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
class DefaultCategoriesTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(DefaultCategories.callback)
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun fiveLockedDefaultsAreSeeded() = runBlocking {
        val all = db.categoryDao().getAll()
        assertEquals(listOf("Work", "Personal", "Ideas", "Health", "Study"), all.map { it.name })
        assertTrue(all.all { it.isDefault })
    }

    @Test
    fun seededDefaultsCannotBeDeletedOrRenamed() = runBlocking {
        val first = db.categoryDao().getAll()[0]
        assertEquals(0, db.categoryDao().deleteCustomById(first.id))
        assertEquals(0, db.categoryDao().renameCustom(first.id, "Hacked"))
        assertEquals("Work", db.categoryDao().getAll()[0].name)
    }
}