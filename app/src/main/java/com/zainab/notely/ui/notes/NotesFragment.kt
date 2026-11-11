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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.zainab.notely.R
import com.zainab.notely.data.local.Category
import com.zainab.notely.databinding.FragmentNotesBinding

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()

    private var categories: List<Category> = emptyList()
    private var totalCount = 0
    private var ignoreChipEvents = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NoteAdapter { note ->
            findNavController().navigate(NotesFragmentDirections.actionNotesToNoteDetail(note.id))
        }
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (ignoreChipEvents) return@setOnCheckedStateChangeListener
            val tag = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it).tag as Int }
            viewModel.setFilter(if (tag == null || tag == ALL) null else tag)
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categories = list
            val filter = viewModel.filterCategoryId.value
            // The selected category no longer exists (for example, it was deleted): back to All
            if (filter != null && list.none { it.id == filter }) viewModel.setFilter(null)
            rebuildChips()
        }

        viewModel.noteCount.observe(viewLifecycleOwner) { count ->
            totalCount = count
            rebuildChips()
        }

        viewModel.notes.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.text = if (viewModel.filterCategoryId.value == null)
                "No notes yet.\nTap + to add one." else "No notes in this category yet."
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                NotesFragmentDirections.actionNotesToAddNote(
                    presetCategoryId = viewModel.filterCategoryId.value ?: -1
                )
            )
        }
    }

    private fun rebuildChips() {
        val selected = viewModel.filterCategoryId.value
        val group = binding.chipGroupFilter
        ignoreChipEvents = true
        group.removeAllViews()
        group.addView(makeChip("All · $totalCount", ALL, selected == null, null))
        categories.forEach { c ->
            group.addView(makeChip(c.name, c.id, selected == c.id, c.colorHex))
        }
        ignoreChipEvents = false
    }

    private fun makeChip(label: String, tagValue: Int, checked: Boolean, colorHex: String?) =
        Chip(requireContext()).apply {
            id = View.generateViewId()
            text = label
            tag = tagValue
            isCheckable = true
            if (colorHex != null) {
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor(colorHex))
                chipStrokeWidth = 3f
            }
            isChecked = checked
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ALL = -1   // tag used by the "All" chip
    }
}