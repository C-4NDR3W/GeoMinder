package com.example.geominder

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.type.Date
import java.util.Locale

class NoteViewFragment : Fragment() {
    private lateinit var toggleButton: ToggleButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var selectedDateTextView: TextView
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val notesList = mutableListOf<Note>()
    private val searchBarVisible = false



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

        noteAdapter = NoteAdapter(emptyList())
        recyclerView.adapter = noteAdapter

        toggleButton = view.findViewById(R.id.toggleButton)

        val toggleState = arguments?.getBoolean("toggleState", false) ?: false
        toggleButton.isChecked = toggleState

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                redirectToMap()
            }
        }

        fetchNotes()
    }

    private fun fetchNotes() {
        val userID = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userID)
            .collection("notes")
            .orderBy("date", Query.Direction.DESCENDING) // Order by date
            .get()
            .addOnSuccessListener { documents ->
                notesList.clear()
                val now = System.currentTimeMillis() // Get current time in milliseconds

                for (document in documents) {
                    val note = document.toObject(Note::class.java)
                    notesList.add(note)
                }

                notesList.sortBy { note ->
                    val dateString = note.date
                    val timeString = note.time

                    // Combine date and time to form a datetime string for comparison
                    val formattedDate = "$dateString $timeString"
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    try {
                        sdf.parse(formattedDate)?.time
                    } catch (e: Exception) {
                        Log.e("NoteViewFragment", "Error parsing date: ${e.message}")
                        0L // If the date parsing fails, return 0L
                    }
                }

                val groupedNotes = groupNotesByDate(notesList)
                noteAdapter = NoteAdapter(groupedNotes)
                recyclerView.adapter = noteAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("NoteViewFragment", "Error fetching notes: ${exception.message}")
                Toast.makeText(requireContext(), "Error fetching notes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun groupNotesByDate(notes: List<Note>): List<Pair<String, List<Note>>> {
        val notesByDate = notes.groupBy { it.date }
        return notesByDate.map { Pair(it.key, it.value) }
    }

    private fun redirectToMap() {
        val navController = findNavController()

        val bundle = Bundle()
        bundle.putBoolean("toggleState", toggleButton.isChecked)

        navController.navigate(R.id.action_noteViewFragment_to_mapFragment, bundle)
    }
}
