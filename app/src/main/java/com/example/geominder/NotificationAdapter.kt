package com.example.geominder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // Ambil `place` berdasarkan `noteId`
        fetchPlace(notification.noteId) { place ->
            holder.placeTextView.text = place
        }

        holder.dateTimeTextView.text = formatDateTime(notification.dateTime)
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun fetchPlace(noteId: String, callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val place = document.getString("place") ?: "Unknown Place"
                    callback(place)
                } else {
                    callback("Unknown Place")
                }
            }
            .addOnFailureListener {
                callback("Unknown Place")
            }
    }


    override fun getItemCount() = notifications.size
}

