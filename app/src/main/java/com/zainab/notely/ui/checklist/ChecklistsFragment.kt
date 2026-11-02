package com.zainab.notely.ui.checklist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zainab.notely.data.local.Checklist
import com.zainab.notely.data.local.ChecklistItem
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.databinding.FragmentChecklistsBinding
import kotlinx.coroutines.launch

class ChecklistsFragment : Fragment() {

    private var _binding: FragmentChecklistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChecklistsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ChecklistAdapter()
        binding.rvChecklists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChecklists.adapter = adapter

        viewModel.allChecklists.observe(viewLifecycleOwner) { adapter.submitList(it) }

        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch { seedIfEmpty(appContext) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TEMPORARY: deleted when the Checklist create flow exists
    private suspend fun seedIfEmpty(context: Context) {
        val db = DatabaseProvider.getDatabase(context)
        if (db.checklistDao().getById(1) == null) {
            val now = System.currentTimeMillis()
            val id = db.checklistDao().insert(
                Checklist(title = "App launch checklist", createdAt = now, updatedAt = now)
            ).toInt()
            db.checklistItemDao().insert(
                ChecklistItem(checklistId = id, text = "Set up Room database", isChecked = true, position = 0))
            db.checklistItemDao().insert(
                ChecklistItem(checklistId = id, text = "Write privacy policy", position = 1))
        }
    }
}