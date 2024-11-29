package com.example.geominder

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GroupViewFragment : Fragment() {

    private lateinit var groupNameTextView: TextView
    private lateinit var memberListRecyclerView: RecyclerView
    private lateinit var editMembersButton: Button
    private lateinit var saveNoteButton: Button
    private lateinit var notesEditText: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var navController: NavController

    private lateinit var groupName : String
    private lateinit var groupId: String
    private lateinit var groupDesc: String
    private lateinit var membersJson : String
    private lateinit var adminId : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val view = inflater.inflate(R.layout.fragment_group_view, container, false)

        groupNameTextView = view.findViewById(R.id.groupNameTextView)
        memberListRecyclerView = view.findViewById(R.id.memberListRecyclerView)
        editMembersButton = view.findViewById(R.id.editMembersButton)
        saveNoteButton = view.findViewById(R.id.saveNoteButton)
        notesEditText = view.findViewById(R.id.notesEditText)

        memberListRecyclerView.layoutManager = LinearLayoutManager(context)

        groupId = arguments?.getString("groupId") ?: ""
        groupName = arguments?.getString("groupName") ?: ""
        adminId = arguments?.getString("adminId") ?: ""
        groupDesc =  arguments?.getString("groupDesc") ?: ""
        membersJson = arguments?.getString("members") ?: ""

        Log.d("GroupViewFragment", "members: $membersJson")
        val gson = Gson()
        val listType = object : TypeToken<List<User>>() {}.type
        val members = gson.fromJson<ArrayList<User>>(membersJson, listType)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)


        if (!checkIsAdmin(adminId))
        {
            editMembersButton.visibility = View.GONE
        }

        loadNotes(groupId)

        saveNoteButton.setOnClickListener {
            saveNotes(groupId, notesEditText.text.toString())
        }

        editMembersButton.setOnClickListener {
            editGroup()
        }

        return view
    }

    private fun checkIsAdmin(adminId : String) : Boolean
    {
        if (auth.currentUser == null || adminId == null) {
            return false
        }

        Log.d("Admin", "Admin ID: $adminId")
        Log.d("User", "User ID: ${auth.currentUser!!.uid}")

        if (auth.currentUser!!.uid != adminId)
        {
            return false
        }

        return true
    }

    private fun loadNotes(groupId: String) {
        val sharedPref = activity?.getSharedPreferences("GroupNotes", Context.MODE_PRIVATE)
        val notesKey = "notes_$groupId"
        val notes = sharedPref?.getString(notesKey, "")
        notesEditText.setText(notes)
    }

    private fun editGroup()
    {
        val bundle = Bundle()
        val action = GroupViewFragmentDirections.actionGroupViewFragmentToGroupEditorFragment(groupName, groupId, adminId, groupDesc, membersJson)

        navController.navigate(action)
    }


    private fun saveNotes(groupId: String, notes: String) {
        val sharedPref = activity?.getSharedPreferences("GroupNotes", Context.MODE_PRIVATE)
        val notesKey = "notes_$groupId"
        sharedPref?.edit()?.apply {
            putString(notesKey, notes)
            apply()
            activity?.runOnUiThread {
                Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show()
    }



}
