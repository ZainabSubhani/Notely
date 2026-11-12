package com.zainab.notely.data.local

data class ChecklistWithProgress(
    val id: Int,
    val title: String,
    val categoryId: Int?,
    val categoryName: String?,
    val categoryColor: String?,
    val totalItems: Int,
    val doneItems: Int
)