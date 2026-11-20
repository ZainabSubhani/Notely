package com.zainab.notely.ui.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.zainab.notely.databinding.FragmentAddReminderBinding
import com.zainab.notely.util.ReminderFormat
import java.util.Calendar
import java.util.TimeZone

class AddReminderFragment : Fragment() {

    private var _binding: FragmentAddReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddReminderViewModel by viewModels()
    private var prefilled = false

    // MaterialDatePicker reports the chosen day as midnight in UTC. Read it with a UTC
    // calendar, or a phone east of UTC can land on the wrong day
    private val onDatePicked = MaterialPickerOnPositiveButtonClickListener<Long> { utcMillis ->
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        viewModel.setDate(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.permissionAnswered = true
            if (!granted) {
                Toast.makeText(
                    requireContext().applicationContext,
                    "Notifications are off, so reminders won't alert you. You can turn them on in Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
            findNavController().popBackStack()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val freshStart = savedInstanceState == null

        if (viewModel.isEditing) {
            binding.btnSave.text = "Save changes"
            binding.btnDelete.visibility = View.VISIBLE
        }

        viewModel.loadedReminder.observe(viewLifecycleOwner) { reminder ->
            if (reminder != null && freshStart && !prefilled) {
                binding.etTitle.setText(reminder.title)
                prefilled = true
            }
        }

        viewModel.dueAt.observe(viewLifecycleOwner) { due ->
            binding.tvDate.text = ReminderFormat.date(due)
            binding.tvTime.text = ReminderFormat.time(due)
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            binding.tvError.text = message
            binding.tvError.visibility = if (message == null) View.GONE else View.VISIBLE
        }

        binding.etTitle.doAfterTextChanged { viewModel.clearError() }
        binding.rowDate.setOnClickListener { showDatePicker() }
        binding.rowTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { viewModel.save(binding.etTitle.text.toString()) }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this reminder?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ -> viewModel.delete() }
                .show()
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) closeAfterSave()
        }

        restorePickerListeners()
    }

    private fun closeAfterSave() {
        // The permission dialog is still open (for example after a rotation): wait for its answer
        if (viewModel.askedForPermission && !viewModel.permissionAnswered) return

        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsPermission && !viewModel.deleted && !viewModel.askedForPermission) {
            viewModel.askedForPermission = true
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date")
            .setSelection(utcMidnightOf(viewModel.dueAt.value ?: System.currentTimeMillis()))
            .build()
        picker.addOnPositiveButtonClickListener(onDatePicked)
        picker.show(childFragmentManager, TAG_DATE)
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = viewModel.dueAt.value ?: System.currentTimeMillis()
        }
        val format = if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(format)
            .setHour(cal.get(Calendar.HOUR_OF_DAY))
            .setMinute(cal.get(Calendar.MINUTE))
            .setTitleText("Pick a time")
            .build()
        picker.addOnPositiveButtonClickListener { viewModel.setTime(picker.hour, picker.minute) }
        picker.show(childFragmentManager, TAG_TIME)
    }

    // After a rotation Android recreates an open picker but not our listener lambdas,
    // so the OK button would do nothing. Re-attach them
    @Suppress("UNCHECKED_CAST")
    private fun restorePickerListeners() {
        (childFragmentManager.findFragmentByTag(TAG_DATE) as? MaterialDatePicker<Long>)
            ?.addOnPositiveButtonClickListener(onDatePicked)
        (childFragmentManager.findFragmentByTag(TAG_TIME) as? MaterialTimePicker)?.let { picker ->
            picker.addOnPositiveButtonClickListener { viewModel.setTime(picker.hour, picker.minute) }
        }
    }

    // Local date to the same calendar day at 00:00 UTC, which is what the picker expects
    private fun utcMidnightOf(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_DATE = "datePicker"
        private const val TAG_TIME = "timePicker"
    }
}