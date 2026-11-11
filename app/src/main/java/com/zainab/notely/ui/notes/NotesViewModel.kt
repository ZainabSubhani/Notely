package com.zainab.notely.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.NoteWithCategory

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val noteDao = db.noteDao()

    private val _filterCategoryId = MutableLiveData<Int?>(null)   // null means "All"
    val filterCategoryId: LiveData<Int?> = _filterCategoryId

    val notes: LiveData<List<NoteWithCategory>> =
        _filterCategoryId.switchMap { id -> noteDao.getFilteredWithCategory(id) }

    val noteCount: LiveData<Int> = noteDao.observeCount()
    val categories: LiveData<List<Category>> = db.categoryDao().observeAll()

    fun setFilter(id: Int?) {
        if (_filterCategoryId.value != id) _filterCategoryId.value = id
    }
}