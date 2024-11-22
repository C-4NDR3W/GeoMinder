package com.example.geominder

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var addGroupButton: ImageView
    private lateinit var navController: NavController
    private lateinit var auth : FirebaseAuth
    private var isNewGroup = true
    private val groups = mutableListOf<Group>()

    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        addGroupButton = view.findViewById(R.id.addGroupButton)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

//        groups.add(Group("Me and the boys", "You, Clay, Javier, Kevin"))
//        groups.add(Group("Mi Familia", "You, Clay, Javier, Kevin"))
//        groups.add(Group("Kevin's party", "You, Clay, Javier, Kevin"))



        groupAdapter = GroupAdapter(groups)
        recyclerView.adapter = groupAdapter

        fetchGroups()

        addGroupButton.setOnClickListener({
            navigateToGroupEditor()
        })

        return view
    }


    fun fetchGroups() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("FetchGroups", "User not logged in")
            return
        }

        // Fetching data from Firestore (for example, inside your fetchGroups function)
        db.collection("groups")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val groupName = document.getString("name") ?: "Unknown"
                    val membersList =
                        document.get("members") as? List<Map<String, Any>> ?: emptyList()

                    val userList = membersList.mapNotNull { member ->
                        val email = member["email"] as? String
                        val userId = member["userId"] as? String
                        if (email != null && userId != null) {
                            User(email, userId)
                        } else {
                            null
                        }
                    }

                    val group = Group(groupName, currentUser.uid, userList)
                    groups.add(group)
                    groupAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FetchGroups", "Error fetching groups: ", exception)
            }

    }


    fun navigateToGroupEditor()
    {
        val bundle = Bundle()
        bundle.putString("type", "newGroup")
       navController.navigate(R.id.action_navigation_group_to_groupEditorFragment)
    }

}
