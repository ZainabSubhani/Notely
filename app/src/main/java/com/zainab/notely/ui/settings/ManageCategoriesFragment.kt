package com.zainab.notely.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zainab.notely.data.local.Category
import com.zainab.notely.databinding.DialogRenameCategoryBinding
import com.zainab.notely.databinding.FragmentManageCategoriesBinding
import com.zainab.notely.ui.common.AddCategoryBottomSheet
import kotlinx.coroutines.launch

class ManageCategoriesFragment : Fragment() {

    private var _binding: FragmentManageCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ManageCategoriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ManageCategoriesAdapter(
            onRename = { showRenameDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.tvAddCategory.setOnClickListener {
            AddCategoryBottomSheet().show(childFragmentManager, AddCategoryBottomSheet.TAG)
        }
    }

    private fun showRenameDialog(category: Category) {
        val dialogBinding = DialogRenameCategoryBinding.inflate(layoutInflater)
        dialogBinding.etRename.setText(category.name)
        dialogBinding.etRename.selectAll()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Rename category")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)   // real click handler is set below
            .create()

        // Set the click handler after show(), so an error keeps the dialog open
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val error = viewModel.rename(category.id, dialogBinding.etRename.text.toString())
                    if (error == null) dialog.dismiss() else dialogBinding.tilRename.error = error
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(category: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete \"${category.name}\"?")
            .setMessage("Notes and checklists in this category will become Uncategorized.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(category.id) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}