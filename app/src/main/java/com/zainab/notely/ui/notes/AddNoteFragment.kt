package com.zainab.notely.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zainab.notely.databinding.FragmentAddNoteBinding

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // After a rotation Android restores what the user typed; do not overwrite it
        val isFreshStart = savedInstanceState == null

        if (viewModel.isEditing) binding.btnSave.text = "Save changes"

        viewModel.loadedNote.observe(viewLifecycleOwner) { note ->
            if (note != null && isFreshStart && !prefilled) {
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
                prefilled = true
            }
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