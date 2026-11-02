package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteSource
import kotlinx.coroutines.launch

class AddNoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = DatabaseProvider.getDatabase(application).noteDao()

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            noteDao.insert(
                Note(
                    title = title.ifBlank { "Untitled" },
                    content = content,
                    createdAt = now,
                    updatedAt = now,
                    sourceType = NoteSource.TYPED
                )
            )
            _saved.value = true
        }
    }
}