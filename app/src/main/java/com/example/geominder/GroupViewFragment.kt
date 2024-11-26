package com.example.geominder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupViewFragment : Fragment() {

    private lateinit var groupNameTextView: TextView
    private lateinit var memberListRecyclerView: RecyclerView
    private lateinit var editMembersButton: Button
    private lateinit var saveNoteButton: Button
    private lateinit var notesEditText: EditText
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_group_view, container, false)

        groupNameTextView = view.findViewById(R.id.groupNameTextView)
        memberListRecyclerView = view.findViewById(R.id.memberListRecyclerView)
        editMembersButton = view.findViewById(R.id.editMembersButton)
        saveNoteButton = view.findViewById(R.id.saveNoteButton)
        notesEditText = view.findViewById(R.id.notesEditText)

        memberListRecyclerView.layoutManager = LinearLayoutManager(context)

        val groupId = arguments?.getString("groupId") ?: ""


        loadNotes(groupId)

        saveNoteButton.setOnClickListener {
            saveNotes(groupId, notesEditText.text.toString())
        }

        return view
    }

    private fun loadNotes(groupId: String) {
        val sharedPref = activity?.getSharedPreferences("GroupNotes", Context.MODE_PRIVATE)
        val notesKey = "notes_$groupId"
        val notes = sharedPref?.getString(notesKey, "")
        notesEditText.setText(notes)
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
