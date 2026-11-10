package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteSource
import kotlinx.coroutines.launch

class AddNoteViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val noteDao = db.noteDao()

    private val noteId: Int = savedStateHandle.get<Int>("noteId") ?: -1
    val isEditing: Boolean = noteId != -1

    val categories: LiveData<List<Category>> = db.categoryDao().observeAll()

    private val _selectedCategoryId = MutableLiveData<Int?>(null)
    val selectedCategoryId: LiveData<Int?> = _selectedCategoryId

    private val _loadedNote = MutableLiveData<Note?>()
    val loadedNote: LiveData<Note?> = _loadedNote

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    init {
        if (isEditing) {
            viewModelScope.launch {
                val note = noteDao.getById(noteId)
                _loadedNote.value = note
                _selectedCategoryId.value = note?.categoryId
            }
        }
    }

    fun selectCategory(id: Int?) {
        if (_selectedCategoryId.value != id) _selectedCategoryId.value = id
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val finalTitle = title.ifBlank { "Untitled" }
            val categoryId = _selectedCategoryId.value
            if (isEditing) {
                val existing = noteDao.getById(noteId)
                if (existing != null) {
                    noteDao.update(
                        existing.copy(
                            title = finalTitle,
                            content = content,
                            categoryId = categoryId,
                            updatedAt = now
                        )
                    )
                }
            } else {
                noteDao.insert(
                    Note(
                        title = finalTitle,
                        content = content,
                        createdAt = now,
                        updatedAt = now,
                        sourceType = NoteSource.TYPED,
                        categoryId = categoryId
                    )
                )
            }
            _saved.value = true
        }
    }
}