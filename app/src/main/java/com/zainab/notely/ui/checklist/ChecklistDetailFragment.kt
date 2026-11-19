package com.zainab.notely.ui.checklist

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.zainab.notely.R
import com.zainab.notely.databinding.FragmentChecklistDetailBinding
import com.zainab.notely.ui.common.makeAddCategoryChip
import kotlinx.coroutines.launch

class ChecklistDetailFragment : Fragment() {

    private var _binding: FragmentChecklistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChecklistDetailViewModel by viewModels()
    private var prefilled = false
    private var ignoreChipEvents = false
    private var lastItemCount = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val freshStart = savedInstanceState == null

        // With the tab bar hidden, this screen owns the bottom edge: keep the add bar
// above the system buttons, or above the keyboard when it is open
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.updatePadding(bottom = maxOf(bars, ime))
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

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
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            val grew = lastItemCount != -1 && items.size > lastItemCount
            lastItemCount = items.size
            // Scroll to the bottom only after the new item is actually in the list
            adapter.submitList(items) {
                if (grew) binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
            }
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

        // Persistent add bar: the keyboard's Done key and the + button both add an item,
        // and the keyboard stays open for quick entry
        binding.btnAddItem.setOnClickListener { addItem() }
        binding.etNewItem.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { addItem(); true } else false
        }

        // The overflow menu only makes sense once the checklist exists
        viewModel.currentId.observe(viewLifecycleOwner) { id ->
            binding.btnMore.visibility = if (id != -1) View.VISIBLE else View.GONE
        }
        binding.btnMore.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                menu.add(0, MENU_DELETE, 0, "Delete checklist")
                setOnMenuItemClickListener {
                    if (it.itemId == MENU_DELETE) confirmDelete()
                    true
                }
                show()
            }
        }

        binding.btnDone.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val title = binding.etTitle.text.toString()
                viewModel.addItemNow(binding.etNewItem.text.toString(), title)
                viewModel.persistTitle(title)
                val saved = viewModel.currentId.value != -1
                val host = requireActivity().findViewById<View>(android.R.id.content)
                findNavController().popBackStack()
                // Show it after going back, so it can sit above the tab bar again
                if (saved) host.post { showSavedSnackbar(host) }
            }
        }
    }

    private fun addItem() {
        viewModel.addItem(binding.etNewItem.text.toString(), binding.etTitle.text.toString())
        binding.etNewItem.text?.clear()
    }

    private fun confirmDelete() {
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

    private fun showSavedSnackbar(host: View) {
        val ctx = host.context
        val snackbar = Snackbar.make(host, "Checklist saved", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(ctx, R.color.accent))
            .setTextColor(ContextCompat.getColor(ctx, R.color.on_accent))
        val nav = host.findViewById<View>(R.id.bottomNav)
        if (nav != null && nav.visibility == View.VISIBLE) snackbar.setAnchorView(nav)
        snackbar.show()
    }

    // Leaving by the back gesture still saves the title
    override fun onStop() {
        super.onStop()
        viewModel.saveTitle(binding.etTitle.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object { private const val MENU_DELETE = 1 }
}