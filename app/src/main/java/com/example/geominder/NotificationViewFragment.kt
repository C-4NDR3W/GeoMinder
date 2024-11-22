package com.example.geominder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationViewFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationsList = mutableListOf<Notification>()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(notificationsList)
        recyclerView.adapter = notificationAdapter

        fetchNotifications()

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                val notification = notificationsList[position]
                deleteNotification(notification, position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun deleteNotification(notification: Notification, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("notifications")
            .whereEqualTo("noteId", notification.noteId)
            .whereEqualTo("dateTime", notification.dateTime)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            notificationsList.removeAt(position)
                            notificationAdapter.notifyItemRemoved(position)
                            Log.d("NotificationViewFragment", "Notification deleted successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("NotificationViewFragment", "Error deleting notification", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationViewFragment", "Error fetching notification for deletion", exception)
            }
    }



    private fun fetchNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("notifications")
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    notificationsList.clear()
                    for (document in documents) {
                        val notification = document.toObject(Notification::class.java)
                        notificationsList.add(notification)
                    }
                    notificationAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("NotificationViewFragment", "Error fetching notifications", exception)
                }
        } else {
            Log.e("NotificationViewFragment", "No user logged in")
        }
    }


//    private fun fetchNotifications() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            firestore.collection("users").document(userId).collection("notes")
//                .orderBy("dateTime")
//                .get()
//                .addOnSuccessListener { documents ->
//                    notificationsList.clear()
//                    for (document in documents) {
//                        val notification = document.toObject(Notification::class.java)
//                        notificationsList.add(notification)
//                    }
//                    notificationAdapter.notifyDataSetChanged()
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("NotificationViewFragment", "Error fetching notifications", exception)
//                }
//        } else {
//            Log.e("NotificationViewFragment", "No user logged in")
//        }
//    }



}

