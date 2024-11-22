package com.example.geominder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    private fun loadGroupDetails(groupName: String) {
        db.collection("groups")
            .whereEqualTo("name", groupName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Firestore", "No groups found with the name: $groupName")
                } else {
                    for (document in documents) {
                        val group = document.toObject(Group::class.java)
                        groupNameTextView.text = group.name
                        // Set other details or proceed with fetching members etc.
                        break // Break after the first match if name is unique
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching group details", exception)
            }
    }

}
