package com.example.geominder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.titleTextView.text = notification.title
        holder.placeTextView.text = notification.place
        holder.dateTimeTextView.text = notification.dateTime
    }

    override fun getItemCount() = notifications.size
}

