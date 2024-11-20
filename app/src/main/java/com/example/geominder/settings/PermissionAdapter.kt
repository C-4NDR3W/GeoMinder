package com.example.geominder.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.geominder.R

class PermissionAdapter(
    private val permissions: List<Permission>,
    private val onToggleClick: (Permission, Boolean) -> Unit
) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    inner class PermissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.permissionName)
        val description: TextView = view.findViewById(R.id.permissionDescription)
        val toggle: SwitchCompat = view.findViewById(R.id.permissionToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.permission_item, parent, false)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = permissions[position]
        holder.name.text = permission.name
        holder.description.text = permission.description
        holder.toggle.isChecked = permission.isGranted

        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            onToggleClick(permission, isChecked)
        }
    }

    override fun getItemCount(): Int = permissions.size
}
