package com.zainab.notely.ui.common

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils

fun bindCategoryTag(accent: View, tag: TextView, name: String?, colorHex: String?) {
    if (name != null && colorHex != null) {
        val color = Color.parseColor(colorHex)
        accent.setBackgroundColor(color)
        tag.text = name.uppercase()
        tag.background = GradientDrawable().apply {
            cornerRadius = 5f * tag.resources.displayMetrics.density
            setColor(ColorUtils.setAlphaComponent(color, 64))
        }
        tag.visibility = View.VISIBLE
    } else {
        accent.setBackgroundColor(Color.TRANSPARENT)
        tag.visibility = View.GONE
    }
}