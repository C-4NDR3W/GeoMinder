package com.example.geominder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(private val groups: List<Group>,
                   private val onGroupClicked: (Group) -> Unit,
                   private val onDeleteClick: (Group) -> Unit
): RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.GroupName)
        val groupDesc: TextView = view.findViewById(R.id.GroupDesc)
        val groupMembers: TextView = view.findViewById(R.id.textViewGroupMembers)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

        fun bind(group: Group,
                 clickListener: (Group) -> Unit,
                 deleteClickListener: (Group) -> Unit
        ) {
            groupName.text = group.name
            groupDesc.text = group.desc

            val memberList = group.members.take(3).joinToString(separator = "\n") { it.email }
            groupMembers.text = memberList

            // Handle item click
            itemView.setOnClickListener { clickListener(group) }

            // Handle delete button click
            deleteButton.setOnClickListener { deleteClickListener(group) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group, onGroupClicked, onDeleteClick)
    }

    override fun getItemCount(): Int = groups.size
}
