package com.example.geominder.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.geominder.R
import com.google.android.gms.maps.MapView

class SettingsFragment : Fragment() {

    private lateinit var profileButton: ConstraintLayout
    private lateinit var locationButton: ConstraintLayout
    private lateinit var mapButton: ConstraintLayout
    private lateinit var dataButton: ConstraintLayout
    private lateinit var helpButton: ConstraintLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileButton = view.findViewById(R.id.profileSettingButton)
        locationButton = view.findViewById(R.id.locationSettingsButton)
        mapButton = view.findViewById(R.id.mapSettingsButton)
        dataButton = view.findViewById(R.id.dataSettingsButton)
        helpButton = view.findViewById(R.id.helpSettingButton)

        profileButton.setOnClickListener{

        }

        locationButton.setOnClickListener{

        }

        mapButton.setOnClickListener{

        }

        dataButton.setOnClickListener{

        }

        helpButton.setOnClickListener{

        }

    }

}