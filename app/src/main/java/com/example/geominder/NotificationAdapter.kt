package com.example.geominder

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationAdapter(private val notifications: List<Notification>, private val context: Context) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("PlacesCache", Context.MODE_PRIVATE)

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

        // Ambil `place` dari cache atau fetch baru jika perlu
        val cachedPlace = sharedPreferences.getString(notification.noteId, null)
        if (cachedPlace != null) {
            holder.placeTextView.text = cachedPlace
        } else {
            fetchPlace(notification.noteId) { place ->
                holder.placeTextView.text = place
                savePlaceToCache(notification.noteId, place)
            }
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
                val place = document.getString("place") ?: "Unknown Place"
                callback(place)
                savePlaceToCache(noteId, place)  // Save to cache when fetched
            }
            .addOnFailureListener {
                callback("Unknown Place")
            }
    }

    private fun savePlaceToCache(noteId: String, place: String) {
        with(sharedPreferences.edit()) {
            putString(noteId, place)
            apply()
        }
    }

    override fun getItemCount() = notifications.size
}
