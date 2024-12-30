package com.example.geominder

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val groupsList = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewGroup)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up adapter
        groupAdapter = GroupAdapter(
            groups = groupsList,
            onGroupClicked = { group -> navigateToGroupEditor(group) },
            onDeleteClick = { group -> deleteGroup(group) }
        )
        recyclerView.adapter = groupAdapter

        val addGroupButton: ImageView = view.findViewById(R.id.addGroupButton)
        addGroupButton.setOnClickListener {
            navigateToGroupEditor(createDefaultGroup())
        }

        fetchGroups()
    }


    private fun fetchGroups() {
        val userId = auth.currentUser?.uid // Get the current logged-in user's ID

        if (userId != null) {
            // Query groups where the user is a member
            firestore.collection("groups")
                .get()
                .addOnSuccessListener { documents ->
                    groupsList.clear()
                    for (document in documents) {
                        val group = document.toObject(Group::class.java)
                        group.id = document.id

                        if (group.members.any { it.userId == userId }) {
                            groupsList.add(group)
                        }
                    }
                    groupAdapter = GroupAdapter(
                        groups = groupsList,
                        onGroupClicked = { group -> navigateToGroupEditor(group) },
                        onDeleteClick = { group -> deleteGroup(group) }
                    )
                    recyclerView.adapter = groupAdapter
                }
                .addOnFailureListener { exception ->
                    Log.e("GroupFragment", "Error fetching groups: ${exception.message}")
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToGroupEditor(group: Group) {
        // Create a bundle with group data
        val bundle = Bundle().apply {
            putString("groupId", group.id)
            putString("groupName", group.name)
            putString("groupDesc", group.desc)
        }

        // Navigate to GroupDetailsFragment (or relevant destination)
        findNavController().navigate(R.id.action_navigation_group_to_groupEditorFragment, bundle)
    }

    private fun createDefaultGroup(): Group {
        // Create and return a default group with empty values
        return Group(
            id = "",  // Empty ID, can be generated later
            name = "",  // Empty group name
            desc = "",  // Empty group description
            members = emptyList()  // Empty members list
        )
    }

    private fun deleteGroup(group: Group) {
        val index = groupsList.indexOf(group)

        firestore.collection("groups").document(group.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "${group.name} deleted", Toast.LENGTH_SHORT).show()
                groupsList.removeAt(index)
                groupAdapter.notifyItemRemoved(index)
            }
            .addOnFailureListener { exception ->
                Log.e("DeleteGroup", "Error deleting group: ", exception)
                Toast.makeText(context, "Error deleting group", Toast.LENGTH_SHORT).show()
            }
    }
}