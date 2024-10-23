package com.example.geominder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore


class NoteCreatorFragment : Fragment() {
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var placeEditText: EditText
    private lateinit var timePickerButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private val firestore = FirebaseFirestore.getInstance()

    private var selectedDateTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_creator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        placeEditText = view.findViewById(R.id.placeEditText)
        timePickerButton = view.findViewById(R.id.timePickerButton)
        saveButton = view.findViewById(R.id.saveButton)
        backButton = view.findViewById(R.id.backButton)

        timePickerButton.setOnClickListener { showDateTimePicker() }
        saveButton.setOnClickListener { saveNote() }
        backButton.setOnClickListener { navigateBack() }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                // Format the selected date and time
                val selectedDateTime = "$dayOfMonth/${month + 1}/$year $hourOfDay:$minute"
                this.selectedDateTime = selectedDateTime
                timePickerButton.text = "Selected: $selectedDateTime"
            }

            // Show the time picker dialog after the date is selected
            TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Show the date picker dialog
        DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val place = placeEditText.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || place.isEmpty() || selectedDateTime == null) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val noteData = hashMapOf(
            "title" to title,
            "content" to content,
            "place" to place,
            "dateTime" to selectedDateTime // Save the selected date and time
        )

        // Save the note under the user identifier "user123"
        firestore.collection("users").document("user123").collection("notes")
            .add(noteData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToNoteView() // Navigate to NoteViewFragment after saving
            }
            .addOnFailureListener { exception ->
                // Log the exception message for debugging
                Log.e("NoteCreatorFragment", "Error saving note: ${exception.message}")
                Toast.makeText(requireContext(), "Error saving note: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToNoteView() {
        findNavController().navigate(R.id.noteViewFragment)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }
}