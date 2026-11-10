package com.zainab.notely.ui.notes

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.zainab.notely.databinding.FragmentAddNoteBinding

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()
    private var prefilled = false
    private var ignoreChipEvents = false   // true while WE change chips, so the listener ignores it

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isFreshStart = savedInstanceState == null
        if (viewModel.isEditing) binding.btnSave.text = "Save changes"

        viewModel.loadedNote.observe(viewLifecycleOwner) { note ->
            if (note != null && isFreshStart && !prefilled) {
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
                prefilled = true
            }
        }

        // The user tapped a chip: tell the ViewModel (no chip selected means Uncategorized)
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (ignoreChipEvents) return@setOnCheckedStateChangeListener
            val id = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it).tag as Int }
            viewModel.selectCategory(id)
        }

        // Build one chip per category, from the database
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            ignoreChipEvents = true
            binding.chipGroupCategory.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    id = View.generateViewId()
                    text = category.name
                    tag = category.id
                    isCheckable = true
                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor(category.colorHex))
                    chipStrokeWidth = 3f
                    isChecked = category.id == viewModel.selectedCategoryId.value
                }
                binding.chipGroupCategory.addView(chip)
            }
            ignoreChipEvents = false
        }

        // The selection changed (for example, an edited note finished loading): tick the right chip
        viewModel.selectedCategoryId.observe(viewLifecycleOwner) { selectedId ->
            ignoreChipEvents = true
            val group = binding.chipGroupCategory
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.isChecked = chip.tag == selectedId
            }
            ignoreChipEvents = false
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (title.isEmpty() && content.isEmpty()) {
                binding.etTitle.error = "Enter a title or some text"
            } else {
                binding.btnSave.isEnabled = false
                viewModel.saveNote(title, content)
            }
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}