package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteWithCategory

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = DatabaseProvider.getDatabase(application).noteDao()

    val allNotes: LiveData<List<NoteWithCategory>> = noteDao.getAllWithCategory()
}