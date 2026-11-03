package com.zainab.notely.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zainab.notely.databinding.FragmentNoteDetailBinding
import java.text.DateFormat
import java.util.Date

class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoteDetailViewModel by viewModels()
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.note.observe(viewLifecycleOwner) { note ->
            if (note != null) {
                binding.llContent.visibility = View.VISIBLE
                binding.tvTitle.text = note.title
                binding.tvContent.text = note.content
                val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(note.updatedAt))
                binding.tvMeta.text = "$date · ${note.sourceType.name.lowercase()}"
            } else {
                binding.llContent.visibility = View.INVISIBLE   // deleted: hide it
            }
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteNote()
            showUndoSnackbar()
        }
    }

    private fun showUndoSnackbar() {
        snackbar = Snackbar.make(binding.root, "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") { viewModel.undoDelete() }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    // Undo was not tapped and we are still on screen: delete stands, leave
                    if (event != DISMISS_EVENT_ACTION && _binding != null) {
                        findNavController().popBackStack()
                    }
                }
            })
        snackbar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null          // must come first, so the callback above skips
        snackbar?.dismiss()
        snackbar = null
    }
}