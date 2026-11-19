package com.zainab.notely.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zainab.notely.databinding.FragmentRemindersBinding

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RemindersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ReminderAdapter(
            onToggle = { reminder, done -> viewModel.setCompleted(reminder, done) },
            onClick = { reminder ->
                findNavController().navigate(
                    RemindersFragmentDirections.actionRemindersToAddReminder(reminderId = reminder.id)
                )
            }
        )
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(RemindersFragmentDirections.actionRemindersToAddReminder())
        }
    }

    // Time passes while the app is in the background: regroup when the user comes back
    override fun onResume() {
        super.onResume()
        viewModel.refreshNow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}