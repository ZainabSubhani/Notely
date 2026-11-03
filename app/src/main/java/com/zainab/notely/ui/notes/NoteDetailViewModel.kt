package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val noteDao = DatabaseProvider.getDatabase(application).noteDao()
    private val noteId: Int = checkNotNull(savedStateHandle["noteId"])

    val note: LiveData<Note?> = noteDao.observeById(noteId)

    private var deletedNote: Note? = null   // kept so Undo can put it back

    fun deleteNote() {
        val current = note.value ?: return
        deletedNote = current
        viewModelScope.launch { noteDao.deleteById(current.id) }
    }

    fun undoDelete() {
        val toRestore = deletedNote ?: return
        deletedNote = null
        viewModelScope.launch { noteDao.insert(toRestore) }
    }
}