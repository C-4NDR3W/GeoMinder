package com.example.geominder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(private val groups: List<Group>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.textViewGroupName)
        val groupMembers: TextView = view.findViewById(R.id.textViewGroupMembers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name
        holder.groupMembers.text = ""

        var membersText = StringBuilder()
        var idx = 0
        for (member : User in group.members) {
            membersText.append(member.email)
            idx++
            if (idx != group.members.size) {
                membersText.append(", ")
            }

            Log.d("GroupAdapter", "Member: ${member.email}")
        }

        holder.groupMembers.text = membersText.toString()
    }


    override fun getItemCount(): Int = groups.size
}


