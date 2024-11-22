package com.example.geominder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(private val groups: List<Group>, private val onItemClick: (Group) -> Unit) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.textViewGroupName)
        val groupMembers: TextView = view.findViewById(R.id.textViewGroupMembers)

        fun bind(group: Group, clickListener: (Group) -> Unit) {
            itemView.setOnClickListener { clickListener(group) }
            groupName.text = group.name
            groupMembers.text = group.members.joinToString(separator = ", ") { it.email }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group, onItemClick)
    }

    override fun getItemCount(): Int = groups.size
}
