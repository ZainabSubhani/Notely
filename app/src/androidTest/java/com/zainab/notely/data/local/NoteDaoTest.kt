package com.zainab.notely.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zainab.notely.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        noteDao = db.noteDao()
        categoryDao = db.categoryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun newNote(categoryId: Int? = null) = Note(
        title = "Test",
        content = "# Hello",
        createdAt = 1L,
        updatedAt = 1L,
        sourceType = NoteSource.TYPED,
        categoryId = categoryId
    )

    @Test
    fun insertAndReadBack() = runBlocking {
        noteDao.insert(newNote())
        val all = noteDao.getAll().getOrAwaitValue()
        assertEquals(1, all.size)
        assertEquals("Test", all[0].title)
        assertEquals(NoteSource.TYPED, all[0].sourceType)
    }

    @Test
    fun getByIdReturnsInsertedNote() = runBlocking {
        val id = noteDao.insert(newNote()).toInt()
        val loaded = noteDao.getById(id)
        assertNotNull(loaded)
        assertEquals("Test", loaded!!.title)
    }

    @Test
    fun deletingCategoryKeepsNoteAndClearsCategoryId() = runBlocking {
        val catId = categoryDao.insert(Category(name = "Temp", colorHex = "#2DD4BF")).toInt()
        val noteId = noteDao.insert(newNote(categoryId = catId)).toInt()

        categoryDao.deleteCustomById(catId)

        val loaded = noteDao.getById(noteId)
        assertNotNull(loaded)               // note survived
        assertNull(loaded!!.categoryId)     // but now "Uncategorized"
    }

    @Test(expected = SQLiteConstraintException::class)
    fun noteWithNonExistentCategoryIsRejected() {
        runBlocking { noteDao.insert(newNote(categoryId = 999)) }
    }

    @Test
    fun observeByIdReturnsNoteAndNullForMissing() = runBlocking {
        val id = noteDao.insert(newNote()).toInt()
        assertEquals("Test", noteDao.observeById(id).getOrAwaitValue()?.title)
        assertNull(noteDao.observeById(999).getOrAwaitValue())
    }

    @Test
    fun deletedNoteCanBeRestoredWithSameId() = runBlocking {
        val id = noteDao.insert(newNote()).toInt()
        val original = noteDao.getById(id)!!

        noteDao.deleteById(id)
        assertNull(noteDao.getById(id))

        noteDao.insert(original)            // same id, so it goes back to the same row
        assertEquals(original, noteDao.getById(id))
    }

    @Test
    fun editedNoteMovesToTopAndKeepsCreatedAtAndSource() = runBlocking {
        val id = noteDao.insert(newNote()).toInt()                       // updatedAt = 1
        noteDao.insert(newNote().copy(title = "Other", updatedAt = 2L))  // newer, so currently first

        val original = noteDao.getById(id)!!
        noteDao.update(original.copy(title = "Edited", updatedAt = 3L))

        val all = noteDao.getAll().getOrAwaitValue()
        assertEquals("Edited", all[0].title)                 // newest update comes first
        assertEquals(1L, all[0].createdAt)                   // creation date untouched
        assertEquals(NoteSource.TYPED, all[0].sourceType)    // source untouched
    }
}