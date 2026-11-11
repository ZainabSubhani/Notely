package com.zainab.notely.data.local

import androidx.room.Embedded

data class NoteWithCategory(
    @Embedded val note: Note,
    val categoryName: String?,
    val categoryColor: String?
)