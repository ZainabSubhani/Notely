package com.zainab.notely.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.DatabaseProvider
import kotlinx.coroutines.launch

class AddCategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).categoryDao()
    private var saving = false   // blocks a double tap from inserting twice

    private val _selectedColor = MutableLiveData(CategoryColors.custom.first())
    val selectedColor: LiveData<String> = _selectedColor

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _done = MutableLiveData(false)
    val done: LiveData<Boolean> = _done

    fun selectColor(hex: String) { _selectedColor.value = hex }

    fun save(rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) { _error.value = "Enter a name"; return }
        if (name.length > MAX_NAME) { _error.value = "Max $MAX_NAME characters"; return }
        if (saving) return
        saving = true
        viewModelScope.launch {
            if (dao.countByName(name) > 0) {
                _error.value = "That name already exists"
                saving = false
            } else {
                dao.insert(Category(name = name, colorHex = _selectedColor.value!!))
                _done.value = true
            }
        }
    }

    companion object { const val MAX_NAME = 20 }
}