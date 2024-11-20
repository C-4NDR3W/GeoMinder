package com.example.geominder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class NoteCreatorFragment : Fragment() {
    private lateinit var noteId: String
    private lateinit var title: String
    private lateinit var content: String
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var place: String

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var placeEditText: EditText
    private lateinit var timePickerButton: TextView

    private lateinit var backButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_creator, container, false)

        // Initialize Firebase and UI components
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        placeEditText = view.findViewById(R.id.placeEditText)
        timePickerButton = view.findViewById(R.id.timePickerButton)
        backButton = view.findViewById(R.id.backButton)
        saveButton = view.findViewById(R.id.saveButton)

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)

        // Hide FAB and Bottom Navigation when on NoteCreatorFragment
        fab.visibility = View.GONE
        bottomNavigationView.visibility = View.GONE

        // Get data from arguments (if any)
        val bundle = requireArguments()
        noteId = bundle.getString("noteId", "")
        title = bundle.getString("title", "")
        content = bundle.getString("content", "")
        date = bundle.getString("date", "")
        time = bundle.getString("time", "")
        place = bundle.getString("place", "")

        Log.d("NoteCreatorFragment", "Editing note with ID: $noteId")
        Log.d("NoteCreatorFragment", "Editing note with Title: $title")
        Log.d("NoteCreatorFragment", "Editing note with Content: $content")
        Log.d("NoteCreatorFragment", "Editing note with Date: $date")
        Log.d("NoteCreatorFragment", "Editing note with Time: $time")
        Log.d("NoteCreatorFragment", "Editing note with Place: $place")

        // Set values if editing an existing note
        titleEditText.setText(title)
        contentEditText.setText(content)
        timePickerButton.text = "Selected: $date $time"
        placeEditText.setText(place)

        // Set up listeners
        timePickerButton.setOnClickListener { showDateTimePicker() }
        backButton.setOnClickListener { navigateBack() }
        saveButton.setOnClickListener { saveNote() }

        return view
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year" // Save selected date

            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                selectedTime = "$hourOfDay:$minute" // Save selected time
                timePickerButton.text = "Selected: $selectedDate $selectedTime"
            }

            TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val place = placeEditText.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || place.isEmpty() || selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val userID = auth.currentUser?.uid ?: return
        val noteRef: DocumentReference

        // If editing an existing note, use the passed noteId
        if (noteId.isNotEmpty()) {
            noteRef = firestore.collection("users")
                .document(userID)
                .collection("notes")
                .document(noteId)

            // Check if the document exists before updating
            noteRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Document exists, update it
                        val noteData = hashMapOf(
                            "id" to noteRef.id,
                            "title" to title,
                            "content" to content,
                            "place" to place,
                            "date" to selectedDate,
                            "time" to selectedTime,
                            "status" to true
                        )

                        noteRef.set(noteData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Note updated successfully!", Toast.LENGTH_SHORT).show()
                                navigateToNoteView()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("NoteCreatorFragment", "Error updating note: ${exception.message}")
                                Toast.makeText(requireContext(), "Error updating note: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Document does not exist
                        Toast.makeText(requireContext(), "Note not found, unable to update.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NoteCreatorFragment", "Error checking note existence: ${exception.message}")
                    Toast.makeText(requireContext(), "Error checking note existence: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If creating a new note, generate a new document reference
            noteRef = firestore.collection("users")
                .document(userID)
                .collection("notes")
                .document()  // Automatically generate a new document ID

            val noteData = hashMapOf(
                "id" to noteRef.id,
                "title" to title,
                "content" to content,
                "place" to place,
                "date" to selectedDate,
                "time" to selectedTime,
                "status" to true
            )

            noteRef.set(noteData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show()
                    navigateToNoteView()
                }
                .addOnFailureListener { exception ->
                    Log.e("NoteCreatorFragment", "Error saving note: ${exception.message}")
                    Toast.makeText(requireContext(), "Error saving note: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToNoteView() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Show FAB and Bottom Navigation when leaving NoteCreatorFragment
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)

        fab.visibility = View.VISIBLE
        bottomNavigationView.visibility = View.VISIBLE
    }
}
