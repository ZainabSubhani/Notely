package com.zainab.notely.ui.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.Checklist
import com.zainab.notely.data.local.ChecklistItem
import com.zainab.notely.data.local.DatabaseProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChecklistDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val checklistDao = db.checklistDao()
    private val itemDao = db.checklistItemDao()
    private val createLock = Mutex()

    private val startId: Int = savedStateHandle.get<Int>("checklistId") ?: -1
    private val presetCategoryId: Int = savedStateHandle.get<Int>("presetCategoryId") ?: -1
    val isNew: Boolean = startId == -1

    // -1 until the checklist exists in the database
    private val _currentId = MutableLiveData(startId)
    val currentId: LiveData<Int> = _currentId

    val checklist: LiveData<Checklist?> = _currentId.switchMap { id ->
        if (id == -1) MutableLiveData<Checklist?>(null) else checklistDao.observeById(id)
    }
    val items: LiveData<List<ChecklistItem>> = _currentId.switchMap { id ->
        if (id == -1) MutableLiveData(emptyList<ChecklistItem>()) else itemDao.observeForChecklist(id)
    }
    val categories: LiveData<List<Category>> = db.categoryDao().observeAll()

    private val _categoryId = MutableLiveData<Int?>(
        if (isNew && presetCategoryId != -1) presetCategoryId else null
    )
    val categoryId: LiveData<Int?> = _categoryId

    init {
        if (!isNew) viewModelScope.launch { _categoryId.value = checklistDao.getById(startId)?.categoryId }
    }

    /** Creates the row on first need. The lock stops two quick taps creating it twice. */
    private suspend fun ensureCreated(title: String): Int = createLock.withLock {
        val existing = _currentId.value ?: -1
        if (existing != -1) return@withLock existing
        val now = System.currentTimeMillis()
        val id = checklistDao.insert(
            Checklist(
                title = title.trim().ifBlank { "Untitled checklist" },
                categoryId = _categoryId.value,
                createdAt = now,
                updatedAt = now
            )
        ).toInt()
        _currentId.value = id
        id
    }

    suspend fun persistTitle(title: String) {
        val id = _currentId.value ?: -1
        if (id == -1) {
            if (title.isNotBlank()) ensureCreated(title)   // blank and no items: save nothing
            return
        }
        val existing = checklistDao.getById(id) ?: return
        val clean = title.trim().ifBlank { "Untitled checklist" }
        if (existing.title != clean) {
            checklistDao.update(existing.copy(title = clean, updatedAt = System.currentTimeMillis()))
        }
    }

    fun saveTitle(title: String) {
        viewModelScope.launch { persistTitle(title) }
    }

    suspend fun addItemNow(text: String, title: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val id = ensureCreated(title)
        itemDao.insert(ChecklistItem(checklistId = id, text = clean, position = itemDao.nextPosition(id)))
    }

    fun addItem(text: String, title: String) {
        viewModelScope.launch { addItemNow(text, title) }
    }

    fun setChecked(item: ChecklistItem, checked: Boolean) {
        viewModelScope.launch { itemDao.setChecked(item.id, checked) }
    }

    fun deleteItem(item: ChecklistItem) {
        viewModelScope.launch { itemDao.deleteById(item.id) }
    }

    fun selectCategory(id: Int?) {
        if (_categoryId.value == id) return
        _categoryId.value = id
        val cid = _currentId.value ?: -1
        if (cid != -1) viewModelScope.launch {
            val c = checklistDao.getById(cid) ?: return@launch
            checklistDao.update(c.copy(categoryId = id, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteChecklist() {
        val id = _currentId.value ?: -1
        if (id != -1) viewModelScope.launch { checklistDao.deleteById(id) }   // items go too (CASCADE)
    }
}