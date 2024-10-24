package com.example.geominder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val groups = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        groups.add(Group("Me and the boys", "You, Clay, Javier, Kevin"))
        groups.add(Group("Mi Familia", "You, Clay, Javier, Kevin"))
        groups.add(Group("Kevin's party", "You, Clay, Javier, Kevin"))

        groupAdapter = GroupAdapter(groups)
        recyclerView.adapter = groupAdapter

        return view
    }
}
