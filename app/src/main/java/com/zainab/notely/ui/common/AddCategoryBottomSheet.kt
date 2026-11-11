package com.zainab.notely.ui.common

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zainab.notely.R
import com.zainab.notely.databinding.SheetAddCategoryBinding

class AddCategoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddCategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val density = resources.displayMetrics.density
        val size = (32 * density).toInt()

        // Six circles, one per palette color
        CategoryColors.custom.forEach { hex ->
            val swatch = View(requireContext()).apply {
                tag = hex
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    marginEnd = (12 * density).toInt()
                }
                setOnClickListener { viewModel.selectColor(hex) }
            }
            binding.llSwatches.addView(swatch)
        }

        viewModel.selectedColor.observe(viewLifecycleOwner) { selected ->
            val ring = ContextCompat.getColor(requireContext(), R.color.text_primary)
            for (i in 0 until binding.llSwatches.childCount) {
                val swatch = binding.llSwatches.getChildAt(i)
                val hex = swatch.tag as String
                swatch.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    if (hex == selected) setStroke((3 * density).toInt(), ring)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { binding.tilName.error = it }
        viewModel.done.observe(viewLifecycleOwner) { if (it) dismiss() }

        binding.etName.doAfterTextChanged { binding.tilName.error = null }
        binding.btnAdd.setOnClickListener { viewModel.save(binding.etName.text.toString()) }

        binding.etName.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object { const val TAG = "AddCategoryBottomSheet" }
}