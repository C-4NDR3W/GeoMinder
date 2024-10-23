package com.example.geominder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class NoteViewFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val notesList = mutableListOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        noteAdapter = NoteAdapter(notesList)
        recyclerView.adapter = noteAdapter

        fetchNotes()
    }

    private fun fetchNotes() {
        firestore.collection("users")
            .document("user123")
            .collection("notes")
            .orderBy("dateTime", Query.Direction.DESCENDING) // Order by date
            .get()
            .addOnSuccessListener { documents ->
                notesList.clear() // Clear the current list
                for (document in documents) {
                    val note = document.toObject(Note::class.java)
                    notesList.add(note)
                }
                noteAdapter.notifyDataSetChanged() // Notify the adapter of the data change
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching notes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
