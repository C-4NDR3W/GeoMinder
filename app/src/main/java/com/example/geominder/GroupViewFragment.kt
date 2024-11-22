package com.example.geominder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupViewFragment : Fragment() {

    private lateinit var groupNameTextView: TextView
    private lateinit var memberListRecyclerView: RecyclerView
    private lateinit var editMembersButton: Button
    private lateinit var addNoteButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_group_view, container, false)

        groupNameTextView = view.findViewById(R.id.groupNameTextView)
        memberListRecyclerView = view.findViewById(R.id.memberListRecyclerView)
        editMembersButton = view.findViewById(R.id.editMembersButton)
        addNoteButton = view.findViewById(R.id.addNoteButton)

        memberListRecyclerView.layoutManager = LinearLayoutManager(context)

        val groupId = arguments?.getString("groupId") ?: ""
        loadGroupDetails(groupId)

        return view
    }

    private fun loadGroupDetails(groupId: String) {
        if (groupId.isEmpty()) {
            Log.d("GroupViewFragment", "No Group ID provided")
            return
        }

        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val group = documentSnapshot.toObject(Group::class.java)
                    groupNameTextView.text = group?.name
                    // Update the UI or RecyclerView adapter based on group data
                } else {
                    Log.d("GroupViewFragment", "No such group with ID: $groupId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GroupViewFragment", "Error fetching group details", e)
            }
    }
}
