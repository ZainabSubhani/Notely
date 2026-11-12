package com.zainab.notely.ui.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.ChecklistWithProgress
import com.zainab.notely.data.local.DatabaseProvider

class ChecklistsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val dao = db.checklistDao()

    private val _filterCategoryId = MutableLiveData<Int?>(null)
    val filterCategoryId: LiveData<Int?> = _filterCategoryId

    val checklists: LiveData<List<ChecklistWithProgress>> =
        _filterCategoryId.switchMap { dao.getFilteredWithProgress(it) }

    val checklistCount: LiveData<Int> = dao.observeCount()
    val categories: LiveData<List<Category>> = db.categoryDao().observeAll()

    fun setFilter(id: Int?) {
        if (_filterCategoryId.value != id) _filterCategoryId.value = id
    }
}