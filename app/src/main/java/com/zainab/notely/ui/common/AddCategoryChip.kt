package com.zainab.notely.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip

fun Fragment.makeAddCategoryChip(): Chip = Chip(requireContext()).apply {
    id = View.generateViewId()
    text = "+ Add"
    isCheckable = false
    setOnClickListener {
        AddCategoryBottomSheet().show(childFragmentManager, AddCategoryBottomSheet.TAG)
    }
}