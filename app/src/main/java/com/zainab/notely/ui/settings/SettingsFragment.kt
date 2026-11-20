package com.zainab.notely.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zainab.notely.R
import com.zainab.notely.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rowManageCategories.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_manageCategories)
        }
        // Opens this app's notification page in the phone's system settings
        binding.rowNotifications.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            )
        }
    }

    // The user may come back from system settings having changed it
    override fun onResume() {
        super.onResume()
        val allowed = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        binding.tvNotificationStatus.text = if (allowed) "Allowed ✓" else "Off · tap to enable"
        binding.tvNotificationStatus.setTextColor(
            ContextCompat.getColor(requireContext(), if (allowed) R.color.accent else R.color.danger)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}