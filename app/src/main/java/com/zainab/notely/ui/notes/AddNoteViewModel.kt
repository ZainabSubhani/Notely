package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteSource
import kotlinx.coroutines.launch

class AddNoteViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val noteDao = DatabaseProvider.getDatabase(application).noteDao()
    private val noteId: Int = savedStateHandle.get<Int>("noteId") ?: -1
    val isEditing: Boolean = noteId != -1

    private val _loadedNote = MutableLiveData<Note?>()
    val loadedNote: LiveData<Note?> = _loadedNote

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    init {
        if (isEditing) {
            viewModelScope.launch { _loadedNote.value = noteDao.getById(noteId) }
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val finalTitle = title.ifBlank { "Untitled" }
            if (isEditing) {
                val existing = noteDao.getById(noteId)
                if (existing != null) {
                    noteDao.update(
                        existing.copy(title = finalTitle, content = content, updatedAt = now)
                    )
                }
            } else {
                noteDao.insert(
                    Note(
                        title = finalTitle,
                        content = content,
                        createdAt = now,
                        updatedAt = now,
                        sourceType = NoteSource.TYPED
                    )
                )
            }
            _saved.value = true
        }
    }
}