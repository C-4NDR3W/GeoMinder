package com.example.geominder

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class NoteViewFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var welcomeMessage: LinearLayout
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
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
        welcomeMessage = view.findViewById(R.id.welcomeMessage)

        val searchBar: EditText = view.findViewById(R.id.searchBar) // Assuming the EditText ID is `searchBar`
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().lowercase(Locale.getDefault())
                filterNotes(searchText)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        noteAdapter = NoteAdapter(emptyList(),
            onNoteClicked = { note ->
            val bundle = Bundle().apply {
                putString("title", note.title)
                putString("content", note.content)
                putString("time", note.time)
                putString("place", note.place)
            }
            findNavController().navigate(R.id.action_noteViewFragment_to_noteCreatorFragment, bundle)
        },
            onDeleteClicked = { note ->
                deleteNote(note)
            },
            onPinClicked = { note ->
                pinNote(note)
            })
        recyclerView.adapter = noteAdapter

        fetchNotes()
    }

    private fun fetchNotes() {
        val userID = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userID)
            .collection("notes")
            .get()
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching notes: ${e.message}")
            }
            .addOnSuccessListener { documents ->
                notesList.clear()
                val now = System.currentTimeMillis() // Get current time in milliseconds

                for (document in documents) {
                    val note = document.toObject(Note::class.java)
                    val isPinned = document.getBoolean("isPinned") ?: false
                    note.isPinned = isPinned

                    val dateString = note.date
                    val timeString = note.time

                    if (dateString != null && timeString != null) {
                        // Combine date and time to get a comparable time in milliseconds
                        val formattedDate = "$dateString $timeString"
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        try {
                            val noteTime = sdf.parse(formattedDate)?.time ?: 0L

                            if (noteTime < now) {
                                // Delete expired note from Firestore
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        Log.d("NoteViewFragment", "Expired note successfully deleted")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("NoteViewFragment", "Failed to delete expired note: ${e.message}")
                                    }
                            } else {
                                // Reformat time to ensure two-digit minutes
                                val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val parsedTime = sdf.parse(formattedDate)
                                note.time = parsedTime?.let { timeSdf.format(it) } ?: timeString

                                // Add non-expired note to the list
                                notesList.add(note)
                            }
                        } catch (e: Exception) {
                            Log.e("NoteViewFragment", "Error parsing date: ${e.message}")
                        }
                    } else {
                        Log.w("NoteViewFragment", "Note date or time is null, skipping.")
                    }
                }

                // Sort notes by combined date and time
                notesList.sortBy { note ->
                    val formattedDate = "${note.date} ${note.time}"
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    sdf.parse(formattedDate)?.time ?: 0L
                }

                val groupedNotes = groupNotes(notesList)
                noteAdapter = NoteAdapter(groupedNotes = groupedNotes,
                    onNoteClicked = { note ->
                        // Handle edit action
                        val bundle = Bundle().apply {
                            putString("noteId", note.id)
                            putString("title", note.title)
                            putString("content", note.content)
                            putString("date", note.date)
                            putString("time", note.time)
                            putString("place", note.place)
                            putString("groupID", note.groupId)
                            putString("groupName", note.groupName)
                        }
                        findNavController().navigate(R.id.action_noteViewFragment_to_noteCreatorFragment, bundle)
                    },
                    onDeleteClicked = { note ->
                        // Handle delete action
                        deleteNote(note)
                    },
                    onPinClicked = { note ->
                        // Handle pin action
                        pinNote(note)
                    })
                recyclerView.adapter = noteAdapter

                if (notesList.isEmpty()) {
                    welcomeMessage.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    welcomeMessage.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NoteViewFragment", "Error fetching notes: ${exception.message}")
                Toast.makeText(requireContext(), "Error fetching notes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun groupNotes(notes: List<Note>): List<Pair<String, List<Note>>> {
        val sortedNotes = notes.sortedWith(compareByDescending<Note> { it.isPinned }
            .thenBy { SimpleDateFormat("dd/MM/yyyy").parse(it.date) })

        // Group by date after sorting
        val notesByDate = sortedNotes.groupBy { it.date }
        return notesByDate.map { (date, notes) -> date to notes }
    }

    private fun deleteNote(note: Note) {
        val userID = auth.currentUser?.uid ?: return

        val noteRef = firestore.collection("users").document(userID).collection("notes").document(note.id)

        noteRef.get().addOnSuccessListener { doc ->
            val groupId = doc.getString("groupId")

            if (groupId != null && groupId != "Personal") {
                firestore.collection("groups").document(groupId).get()
                    .addOnSuccessListener { document ->
                        val members = document.get("members") as? List<HashMap<String, String>> ?: emptyList()

                        members.forEach { member ->
                            val memberUserId = member["userId"]
                            if (memberUserId != null) {
                                firestore.collection("users").document(memberUserId).collection("notes").document(note.id).delete()
                                    .addOnSuccessListener {
                                        Log.d("NoteViewFragment", "Note deleted from member $memberUserId")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("NoteViewFragment", "Failed to delete note from member $memberUserId: ${e.message}")
                                    }
                            }
                        }

                        deleteNoteFromCurrentUser(noteRef, note)
                    }
                    .addOnFailureListener { e ->
                        Log.e("NoteViewFragment", "Failed to fetch group details: ${e.message}")
                        deleteNoteFromCurrentUser(noteRef, note)
                    }
            } else {
                deleteNoteFromCurrentUser(noteRef, note)
            }
        }.addOnFailureListener { e ->
            Log.e("NoteViewFragment", "Failed to get note details: ${e.message}")
            Toast.makeText(requireContext(), "Failed to get note details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNoteFromCurrentUser(noteRef: DocumentReference, note: Note) {
        noteRef.delete().addOnSuccessListener {
            Toast.makeText(requireContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show()
            notesList.remove(note)
            fetchNotes()
        }.addOnFailureListener { e ->
            Log.e("NoteViewFragment", "Failed to delete note: ${e.message}")
            Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pinNote(note: Note) {
        val userID = auth.currentUser?.uid ?: return

        val noteRef = firestore.collection("users")
            .document(userID)
            .collection("notes")
            .document(note.id)

        // Retrieve the current value of isPinned
        noteRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentIsPinned = document.getBoolean("isPinned") ?: false
                    val newIsPinned = !currentIsPinned

                    // Update the value to the opposite
                    noteRef.update("isPinned", newIsPinned)
                        .addOnSuccessListener {
                            val message = if (newIsPinned) {
                                "Note pinned successfully"
                            } else {
                                "Note unpinned successfully"
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            fetchNotes()
                        }
                        .addOnFailureListener { e ->
                            Log.e("NoteViewFragment", "Failed to toggle pin status: ${e.message}")
                            Toast.makeText(requireContext(), "Failed to update pin status", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

    private fun filterNotes(query: String) {
        val filteredNotes = if (query.isEmpty()) {
            notesList // Show all notes if the query is empty
        } else {
            notesList.filter { note ->
                note.title.lowercase(Locale.getDefault()).contains(query) ||
                        note.content.lowercase(Locale.getDefault()).contains(query) ||
                        note.place?.lowercase(Locale.getDefault())?.contains(query) == true
            }
        }

        val groupedNotes = groupNotes(filteredNotes)
        noteAdapter.updateNotes(groupedNotes)
    }
}
