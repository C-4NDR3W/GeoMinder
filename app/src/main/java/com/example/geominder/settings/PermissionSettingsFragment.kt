package com.example.geominder.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geominder.R

class PermissionSettingsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PermissionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_permission_settings, container, false)
        recyclerView = view.findViewById(R.id.permissionsRecyclerView)

        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        val permissions = getPermissionData()
        adapter = PermissionAdapter(permissions) { permission, isChecked ->
            handlePermissionToggle(permission, isChecked)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun getPermissionData(): List<Permission> {
        return listOf(
            Permission(
                "Notification",
                "Allows Notifications",
                isGranted = isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)//prolly should do something about these
            ),
            Permission(
                "Location",
                "Allows access to your location",
                isGranted = isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ),
            Permission(
                "Camera",
                "Allows access to your device camera",
                isGranted = isPermissionGranted(Manifest.permission.CAMERA)
            ),
            Permission(
                "Photos",
                "Allows access to your device photos",
                isGranted = isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)
            )

        )
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handlePermissionToggle(permission: Permission, isChecked: Boolean) {
        if (isChecked) {
            requestPermission(permission.name)
        } else {
            Toast.makeText(
                requireContext(),
                "Cannot revoke permissions programmatically.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestPermission(permission: String) {
        // Trigger permission request dialog.
        requestPermissions(arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((index, permission) in permissions.withIndex()) {
                val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    Toast.makeText(requireContext(), "$permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "$permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

}
