package com.zainab.notely.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.Category
import com.zainab.notely.data.local.DatabaseProvider
import kotlinx.coroutines.launch

sealed class CategoryListItem {
    data class Header(val title: String) : CategoryListItem()
    data class Row(val category: Category) : CategoryListItem()
}

class ManageCategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).categoryDao()

    val items: LiveData<List<CategoryListItem>> = dao.observeAll().map { all ->
        val defaults = all.filter { it.isDefault }
        val custom = all.filter { !it.isDefault }
        buildList {
            add(CategoryListItem.Header("DEFAULT · LOCKED"))
            defaults.forEach { add(CategoryListItem.Row(it)) }
            if (custom.isNotEmpty()) {
                add(CategoryListItem.Header("CUSTOM"))
                custom.forEach { add(CategoryListItem.Row(it)) }
            }
        }
    }

    /** Returns an error message, or null if the rename succeeded. */
    suspend fun rename(id: Int, rawName: String): String? {
        val name = rawName.trim()
        if (name.isEmpty()) return "Enter a name"
        if (name.length > 20) return "Max 20 characters"
        if (dao.countByNameExcludingId(name, id) > 0) return "That name already exists"
        dao.renameCustom(id, name)
        return null
    }

    fun delete(id: Int) {
        viewModelScope.launch { dao.deleteCustomById(id) }
    }
}