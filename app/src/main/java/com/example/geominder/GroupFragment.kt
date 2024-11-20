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

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var addGroupButton: ImageView
    private lateinit var navController: NavController
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

        groups.add(Group("Me and the boys", "You, Clay, Javier, Kevin"))
        groups.add(Group("Mi Familia", "You, Clay, Javier, Kevin"))
        groups.add(Group("Kevin's party", "You, Clay, Javier, Kevin"))

        groupAdapter = GroupAdapter(groups)
        recyclerView.adapter = groupAdapter

        addGroupButton.setOnClickListener({
            navigateToGroupEditor()
        })

        return view
    }

    fun navigateToGroupEditor()
    {
        val bundle = Bundle()
        bundle.putString("type", "newGroup")
       navController.navigate(R.id.action_navigation_group_to_groupEditorFragment)
    }

}
