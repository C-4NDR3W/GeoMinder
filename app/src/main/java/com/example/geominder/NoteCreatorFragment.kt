package com.example.geominder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NoteCreatorFragment : Fragment() {
    private lateinit var noteId: String
    private lateinit var title: String
    private lateinit var content: String
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var place: String

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var placeTextView: TextView
    private lateinit var timePickerButton: TextView

    private lateinit var backButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var groupSpinner: Spinner
    private var groupList = mutableListOf<String>()
    private var groupIdList = mutableListOf<String>()


    private var selectedDate: String? = null
    private var selectedTime: String? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    //test

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_creator, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        placeTextView = view.findViewById(R.id.placeTextView)
        timePickerButton = view.findViewById(R.id.timePickerButton)
        backButton = view.findViewById(R.id.backButton)
        saveButton = view.findViewById(R.id.saveButton)

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)
        fab.visibility = View.GONE
        bottomNavigationView.visibility = View.GONE

        val bundle = requireArguments()
        noteId = bundle.getString("noteId", "")
        title = bundle.getString("title", "")
        content = bundle.getString("content", "")
        date = bundle.getString("date", "")
        time = bundle.getString("time", "")
        place = bundle.getString("place", "")

        titleEditText.setText(title)
        contentEditText.setText(content)
        placeTextView.setText(place)

        if (noteId.isNotEmpty()) {
            loadNoteDataFromFirebase(noteId)
        } else {
            titleEditText.setText(title)
            contentEditText.setText(content)
            placeTextView.setText(place)
        }

        groupSpinner = view.findViewById(R.id.groupSpinner)
        loadGroups()


        timePickerButton.setOnClickListener { showDateTimePicker() }
        backButton.setOnClickListener { navigateBack() }
        saveButton.setOnClickListener { saveNote() }

        arguments?.let { args ->
            val placeName = args.getString("placeName", "")
            val placeAddress = args.getString("placeAddress", "")
            latitude = args.getDouble("latitude", 0.0)
            longitude = args.getDouble("longitude", 0.0)

            if (placeName.isNotEmpty()) {
                placeTextView.setText("$placeName - $placeAddress")
            }
        }

        placeTextView.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.mapFragment)
        }

        return view
    }

    private fun loadNoteDataFromFirebase(noteId: String) {
        val userID = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userID)
            .collection("notes").document(noteId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val place = document.getString("place") ?: ""
                    val date = document.getString("date") ?: ""
                    val time = document.getString("time") ?: ""

                    titleEditText.setText(title)
                    contentEditText.setText(content)
                    placeTextView.setText(place)
                    selectedDate = date
                    selectedTime = time

                    if (date.isNotEmpty() && time.isNotEmpty()) {
                        val timeParts = time.split(":")
                        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
                        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

                        // Format the time using %02d for hour and minute
                        val formattedTime = String.format("%02d:%02d", hour, minute)

                        // Set the text with formatted time
                        timePickerButton.text = "$date, $formattedTime"
                        timePickerButton.setTextColor(resources.getColor(android.R.color.black))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NoteCreatorFragment", "Error loading note: ${exception.message}")
                Toast.makeText(requireContext(), "Error loading note: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                timePickerButton.text = "$selectedDate, $selectedTime"
                timePickerButton.setTextColor(resources.getColor(android.R.color.black))
            }
            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val place = placeTextView.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || place.isEmpty() || selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val userID = auth.currentUser?.uid ?: return

        val noteRef: DocumentReference = firestore.collection("users").document(userID).collection("notes").document(noteId.ifEmpty { firestore.collection("users").document(userID).collection("notes").document().id })

        val selectedGroupPosition = groupSpinner.selectedItemPosition
        val selectedGroupId = if (selectedGroupPosition > 0) groupIdList[selectedGroupPosition] else "Personal"

        val noteData: HashMap<String, Any?> = hashMapOf(
            "id" to noteRef.id,
            "title" to title,
            "content" to content,
            "place" to place,
            "latitude" to latitude,
            "longitude" to longitude,
            "date" to selectedDate,
            "time" to selectedTime,
            "status" to true,
            "isPinned" to false,
            "groupId" to selectedGroupId
        )

        noteRef.set(noteData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show()
                scheduleNotification(noteRef.id, noteTitle = title)
                navigateToNoteView()
                if (selectedGroupId != "Personal") {
                    shareNoteWithGroup(noteRef, noteData, selectedGroupId, userID)
                } else {
                    Toast.makeText(requireContext(), "Note saved as personal!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NoteCreatorFragment", "Error saving note: ${exception.message}")
                Toast.makeText(requireContext(), "Error saving note: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun shareNoteWithGroup(noteRef: DocumentReference, noteData: HashMap<String, Any?>, groupId: String, userId: String) {
        firestore.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                val members = document.get("members") as? List<HashMap<String, String>> ?: emptyList()

                for (member in members) {
                    val memberUserId = member["userId"]
                    if (memberUserId != null && memberUserId != userId) {
                        val memberNoteRef = firestore.collection("users").document(memberUserId).collection("notes").document(noteRef.id)
                        memberNoteRef.set(noteData)
                            .addOnSuccessListener {
                                Log.d("NoteCreatorFragment", "Note shared with user: $memberUserId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("NoteCreatorFragment", "Failed to share note with user: $memberUserId", e)
                            }
                    }
                }
                Toast.makeText(requireContext(), "Note shared with group members!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("NoteCreatorFragment", "Failed to fetch group members: ${e.message}", e)
                Toast.makeText(requireContext(), "Error sharing note with group.", Toast.LENGTH_SHORT).show()
            }
    }




    private fun scheduleNotification(noteId: String, noteTitle: String) {
        val noteRef = firestore.collection("users").document(auth.currentUser?.uid ?: return).collection("notes").document(noteId)
        noteRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val noteTime = documentSnapshot.getString("time") ?: return@addOnSuccessListener
                val noteDate = documentSnapshot.getString("date") ?: return@addOnSuccessListener

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = sdf.parse("$noteDate $noteTime") ?: return@addOnSuccessListener
                val reminderTime = date.time - TimeUnit.MINUTES.toMillis(15)
                val delay = reminderTime - System.currentTimeMillis()

                if (delay > 0) {
                    val inputData = Data.Builder()
                        .putString("noteTitle", noteTitle)
                        .putString("noteId", noteId)
                        .build()

                    val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(requireContext()).enqueue(reminderRequest)
                }
            }
        }
    }

    private fun loadGroups() {
        val userID = auth.currentUser?.uid ?: return

        firestore.collection("groups")
            .whereEqualTo("admin", userID)
            .get()
            .addOnSuccessListener { documents ->
                groupList.clear()
                groupIdList.clear()
                groupList.add("Select a Group")
                groupIdList.add("")

                for (document in documents) {
                    val groupName = document.getString("name") ?: "Unnamed Group"
                    val groupId = document.id
                    groupList.add(groupName)
                    groupIdList.add(groupId)
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, groupList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                groupSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load groups: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("NoteCreatorFragment", "Error loading groups: ${exception.message}", exception)
            }
    }

    private fun navigateToNoteView() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun navigateToMapView()
    {

        val bundle = Bundle()
        bundle.putString("noteId", noteId)
        findNavController().navigate(R.id.action_navigation_create_to_mapFragment, bundle)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)
        fab.visibility = View.VISIBLE
        bottomNavigationView.visibility = View.VISIBLE
    }
}
