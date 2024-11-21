package com.example.geominder.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.credentials.provider.Action
import com.example.geominder.R
import com.google.firebase.BuildConfig

class HelpSettingFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_help_setting, container, false)
        val aboutProject = view.findViewById<TextView>(R.id.about_project)
        val versionCode = view.findViewById<TextView>(R.id.version_number)
        val versionValue = BuildConfig.VERSION_NAME

        versionCode.text = versionValue


        aboutProject.setOnClickListener{
            val url = "https://github.com/C-4NDR3W/GeoMinder"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }

        versionCode.text = versionValue
        return view
    }

}