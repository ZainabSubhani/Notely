package com.zainab.notely.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object DefaultCategories {

    private val defaults = listOf(
        "Work" to "#6366F1",
        "Personal" to "#FB7185",
        "Ideas" to "#A78BFA",
        "Health" to "#34D399",
        "Study" to "#38BDF8"
    )

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            defaults.forEach { (name, color) ->
                db.execSQL(
                    "INSERT INTO categories (name, colorHex, isDefault) VALUES (?, ?, 1)",
                    arrayOf(name, color)
                )
            }
        }
    }
}