package com.example.geominder.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.geominder.R

class SettingsFragment : Fragment() {

    private lateinit var profileButton: ConstraintLayout
    private lateinit var permissionButton: ConstraintLayout
    private lateinit var mapButton: ConstraintLayout
    private lateinit var dataButton: ConstraintLayout
    private lateinit var helpButton: ConstraintLayout
    private lateinit var appearanceButton: ConstraintLayout
    private lateinit var notificationButton: ConstraintLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Settings Fragment Called", "Settings Fragment is called")
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileButton = view.findViewById(R.id.profileSettingButton)
        permissionButton = view.findViewById(R.id.permissionsSettingButton)
        mapButton = view.findViewById(R.id.mapSettingButton)
        dataButton = view.findViewById(R.id.dataSettingButton)
        helpButton = view.findViewById(R.id.helpSettingButton)
        appearanceButton = view.findViewById(R.id.appearanceSettingButton)
        notificationButton = view.findViewById(R.id.notificationSettingButton)

        profileButton.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_settings_to_settings_navigation_profile)
        }

        permissionButton.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_settings_to_settings_navigation_permission)
        }

        mapButton.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_settings_to_settings_navigation_map)
        }

        dataButton.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_settings_to_settings_navigation_data)
        }

        helpButton.setOnClickListener{

        }

        appearanceButton.setOnClickListener{

        }

        notificationButton.setOnClickListener{

        }

    }

}