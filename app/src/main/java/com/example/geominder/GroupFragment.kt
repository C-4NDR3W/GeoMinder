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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.gson.Gson

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var addGroupButton: ImageView
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private val groups = mutableListOf<Group>()

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        // Initialize UI components
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        recyclerView = view.findViewById(R.id.recyclerView)
        addGroupButton = view.findViewById(R.id.addGroupButton)

        recyclerView.layoutManager = LinearLayoutManager(context)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Adapter with onDeleteClick listener
        groupAdapter = GroupAdapter(
            groups = groups,
            onItemClick = { group ->
                val gson = Gson()
                val members = gson.toJson(group.members)
                Log.d("GroupFragment", "group id: ${group.id}, members: $members")
            },
            onDeleteClick = { group ->
                deleteGroup(group)
            }
        )

        recyclerView.adapter = groupAdapter

        // Fetch groups from Firestore
        fetchGroups()

        addGroupButton.setOnClickListener {
            navigateToGroupEditor()
        }

        return view
    }

    private fun fetchGroups() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.d("FetchGroups", "User not logged in")
            return
        }

        db.collection("groups").where(
            Filter.or(
                Filter.arrayContains("members", currentUser.uid),
                Filter.equalTo("admin", currentUser.uid)
            )
        )
            .get()
            .addOnSuccessListener { documents ->
                groups.clear() // Clear previous data
                for (document in documents) {
                    val groupName = document.getString("name") ?: "Unknown"
                    val groupDesc = document.getString("desc") ?: "No description"
                    val groupAdmin = document.getString("admin") ?: "Unknown"
                    val groupId = document.id
                    val membersList =
                        document.get("members") as? List<Map<String, Any>> ?: emptyList()

                    val userList = membersList.mapNotNull { member ->
                        val email = member["email"] as? String
                        val userId = member["userId"] as? String
                        if (email != null && userId != null) User(email, userId) else null
                    }

                    val group = Group(groupName, groupAdmin, groupId, groupDesc, userList)
                    groups.add(group)
                }
                groupAdapter.notifyDataSetChanged() // Notify adapter once after all groups are fetched
            }
            .addOnFailureListener { exception ->
                Log.e("FetchGroups", "Error fetching groups: ", exception)
                Toast.makeText(context, "Error fetching groups", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteGroup(group: Group) {
        db.collection("groups").document(group.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "${group.name} deleted", Toast.LENGTH_SHORT).show()
                groups.remove(group) // Remove from local list
                groupAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("DeleteGroup", "Error deleting group: ", exception)
                Toast.makeText(context, "Error deleting group", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToGroupEditor() {
        val bundle = Bundle()
        bundle.putString("type", "newGroup")
        navController.navigate(R.id.action_navigation_group_to_groupEditorFragment, bundle)
    }
}