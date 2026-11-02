package com.zainab.notely.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteSource
import com.zainab.notely.databinding.FragmentNotesBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = NoteAdapter()
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        viewModel.allNotes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch { seedIfEmpty(appContext) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TEMPORARY: deleted when the Add Note screen exists
    private suspend fun seedIfEmpty(context: Context) {
        val dao = DatabaseProvider.getDatabase(context).noteDao()
        if (dao.getById(1) == null) {
            val now = System.currentTimeMillis()
            dao.insert(Note(title = "Meeting notes", content = "Discussed Q3 roadmap",
                createdAt = now, updatedAt = now, sourceType = NoteSource.TYPED))
            dao.insert(Note(title = "Lecture pg 3", content = "Mitochondria is the powerhouse of the cell",
                createdAt = now, updatedAt = now, sourceType = NoteSource.SCANNED))
        }
    }
}