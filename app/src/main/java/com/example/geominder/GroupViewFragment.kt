package com.example.geominder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class GroupViewFragment : Fragment() {

    private lateinit var groupNameTextView: TextView
    private lateinit var memberListRecyclerView: RecyclerView
    private lateinit var editMembersButton: Button
    private lateinit var saveNoteButton: Button
    private lateinit var notesEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var placeEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var timePickerButton: TextView  // Time picker button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var navController: NavController

    private lateinit var groupName : String
    private lateinit var groupId: String
    private lateinit var groupDesc: String
    private lateinit var membersJson : String
    private lateinit var adminId : String

    private val SHARED_PREFS_KEY = "GroupNotes"
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val view = inflater.inflate(R.layout.fragment_group_view, container, false)

        groupNameTextView = view.findViewById(R.id.groupNameTextView)
        memberListRecyclerView = view.findViewById(R.id.memberListRecyclerView)
        editMembersButton = view.findViewById(R.id.editMembersButton)
        saveNoteButton = view.findViewById(R.id.saveNoteButton)
//        notesEditText = view.findViewById(R.id.notesEditText)

        titleEditText = view.findViewById(R.id.titleEditText)
        placeEditText = view.findViewById(R.id.placeEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        timePickerButton = view.findViewById(R.id.timePickerButton)

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

        if (!checkIsAdmin(adminId)) {
            editMembersButton.visibility = View.GONE
        }

        loadNotes(groupId)

        saveNoteButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val place = placeEditText.text.toString().trim()
            val details = contentEditText.text.toString().trim()
            val time = timePickerButton.text.toString()

            if (title.isNotEmpty() && place.isNotEmpty() && details.isNotEmpty() && time != "Select Date and Time") {
                saveNoteToSharedPreferences(title, place, details, time)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }


        timePickerButton.setOnClickListener {
            showDateTimePicker()
        }

        editMembersButton.setOnClickListener {
            editGroup()
        }

        return view
    }

    private fun checkIsAdmin(adminId: String): Boolean {
        if (auth.currentUser == null || adminId == null) {
            return false
        }

        Log.d("Admin", "Admin ID: $adminId")
        Log.d("User", "User ID: ${auth.currentUser!!.uid}")

        return auth.currentUser!!.uid == adminId
    }

    private fun loadNotes(groupId: String) {
        val sharedPref = activity?.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        val notesKey = "notes_$groupId"
        val noteContent = sharedPref?.getString(notesKey, "")
        if (!noteContent.isNullOrEmpty()) {
            val parts = noteContent.split("\n")
            if (parts.size >= 4) {
                titleEditText.setText(parts[0].removePrefix("Title: ").trim())
                placeEditText.setText(parts[1].removePrefix("Place: ").trim())
                contentEditText.setText(parts[2].removePrefix("Details: ").trim())
                timePickerButton.text = parts[3].removePrefix("Time: ").trim()
            }
        }
    }

    private fun saveNoteToSharedPreferences(title: String, place: String, details: String, time: String) {
        val sharedPref = activity?.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        val notesKey = "notes_$groupId"
        val noteContent = "Title: $title\nPlace: $place\nDetails: $details\nTime: $time"

        sharedPref?.edit()?.apply {
            putString(notesKey, noteContent)
            apply()

            activity?.runOnUiThread {
                Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"

            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                selectedTime = "$hourOfDay:$minute"
                timePickerButton.text = "Selected: $selectedDate $selectedTime"
            }

            TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun editGroup() {
        val bundle = Bundle()
        val action = GroupViewFragmentDirections.actionGroupViewFragmentToGroupEditorFragment(groupName, groupId, adminId, groupDesc, membersJson)

        navController.navigate(action)
    }
}
