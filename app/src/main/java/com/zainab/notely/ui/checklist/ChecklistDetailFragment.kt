package com.zainab.notely.ui.checklist

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zainab.notely.databinding.FragmentChecklistDetailBinding
import com.zainab.notely.ui.common.makeAddCategoryChip
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ChecklistDetailFragment : Fragment() {

    private var _binding: FragmentChecklistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChecklistDetailViewModel by viewModels()
    private var prefilled = false
    private var ignoreChipEvents = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val freshStart = savedInstanceState == null

        val adapter = ChecklistItemAdapter(
            onToggle = { item, checked -> viewModel.setChecked(item, checked) },
            onRemove = { viewModel.deleteItem(it) }
        )
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        viewModel.checklist.observe(viewLifecycleOwner) { cl ->
            if (cl != null && freshStart && !prefilled && !viewModel.isNew) {
                binding.etTitle.setText(cl.title)
                prefilled = true
            }

            binding.btnDone.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val title = binding.etTitle.text.toString()
                    // An item typed but not yet added would be lost, so add it first
                    viewModel.addItemNow(binding.etNewItem.text.toString(), title)
                    viewModel.persistTitle(title)
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val done = items.count { it.isChecked }
            binding.progressBar.progress = if (items.isEmpty()) 0 else done * 100 / items.size
            binding.tvProgress.text = "$done of ${items.size} done"
        }

        // Category chips: same pattern as the Add Note screen
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (ignoreChipEvents) return@setOnCheckedStateChangeListener
            viewModel.selectCategory(checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it).tag as Int })
        }
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            ignoreChipEvents = true
            binding.chipGroupCategory.removeAllViews()
            categories.forEach { c ->
                binding.chipGroupCategory.addView(Chip(requireContext()).apply {
                    id = View.generateViewId()
                    text = c.name
                    tag = c.id
                    isCheckable = true
                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor(c.colorHex))
                    chipStrokeWidth = 3f
                    isChecked = c.id == viewModel.categoryId.value
                })
            }
            binding.chipGroupCategory.addView(makeAddCategoryChip())
            ignoreChipEvents = false
        }
        viewModel.categoryId.observe(viewLifecycleOwner) { selectedId ->
            ignoreChipEvents = true
            val group = binding.chipGroupCategory
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.isChecked = chip.tag == selectedId
            }
            ignoreChipEvents = false
        }

        binding.btnAddItem.setOnClickListener { addItem() }
        binding.etNewItem.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { addItem(); true } else false
        }

        viewModel.currentId.observe(viewLifecycleOwner) { id ->
            binding.btnDeleteChecklist.visibility = if (id != -1) View.VISIBLE else View.GONE
        }
        binding.btnDeleteChecklist.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this checklist?")
                .setMessage("Its items will be deleted too.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteChecklist()
                    findNavController().popBackStack()
                }
                .show()
        }
    }

    private fun addItem() {
        viewModel.addItem(binding.etNewItem.text.toString(), binding.etTitle.text.toString())
        binding.etNewItem.text?.clear()
    }

    // Leaving the screen saves the title. There is no separate Save button on this screen
    override fun onStop() {
        super.onStop()
        viewModel.saveTitle(binding.etTitle.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}