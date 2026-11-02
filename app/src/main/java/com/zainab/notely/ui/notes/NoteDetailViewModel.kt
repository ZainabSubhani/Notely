package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note

class NoteDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val noteId: Int = checkNotNull(savedStateHandle["noteId"])

    val note: LiveData<Note?> =
        DatabaseProvider.getDatabase(application).noteDao().observeById(noteId)
}