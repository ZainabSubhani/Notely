package com.zainab.notely.ui.checklist

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
import com.zainab.notely.data.local.Category
import com.zainab.notely.databinding.FragmentChecklistsBinding
import com.zainab.notely.ui.common.makeAddCategoryChip

class ChecklistsFragment : Fragment() {

    private var _binding: FragmentChecklistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChecklistsViewModel by viewModels()

    private var categories: List<Category> = emptyList()
    private var totalCount = 0
    private var ignoreChipEvents = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ChecklistAdapter { item ->
            findNavController().navigate(
                ChecklistsFragmentDirections.actionChecklistsToChecklistDetail(checklistId = item.id)
            )
        }
        binding.rvChecklists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChecklists.adapter = adapter

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (ignoreChipEvents) return@setOnCheckedStateChangeListener
            val tag = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it).tag as Int }
            viewModel.setFilter(if (tag == null || tag == ALL) null else tag)
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categories = list
            val filter = viewModel.filterCategoryId.value
            if (filter != null && list.none { it.id == filter }) viewModel.setFilter(null)
            rebuildChips()
        }

        viewModel.checklistCount.observe(viewLifecycleOwner) { count ->
            totalCount = count
            rebuildChips()
        }

        viewModel.checklists.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.text = if (viewModel.filterCategoryId.value == null)
                "No checklists yet.\nTap + to add one." else "No checklists in this category yet."
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                ChecklistsFragmentDirections.actionChecklistsToChecklistDetail(
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
        categories.forEach { c -> group.addView(makeChip(c.name, c.id, selected == c.id, c.colorHex)) }
        group.addView(makeAddCategoryChip())
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

    companion object { private const val ALL = -1 }
}