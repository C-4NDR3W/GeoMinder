package com.example.geominder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupEditorFragment : Fragment() {
    private lateinit var groupId: String
    private lateinit var groupName: String
    private lateinit var groupDescription: String

    private lateinit var groupNameEditText: EditText
    private lateinit var groupDescriptionEditText: EditText
    private lateinit var saveButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var suggestionField: AutoCompleteTextView
    private lateinit var userListField: ListView
    private var addedUsers = mutableListOf<User>()
    private var suggestions = mutableListOf<String>()
    private lateinit var userListsAdapter: ArrayAdapter<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_editor, container, false)

        groupNameEditText = view.findViewById(R.id.groupNameField)
        groupDescriptionEditText = view.findViewById(R.id.groupDescField)
        saveButton = view.findViewById(R.id.groupActionButton)
        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener { navigateBack() }

        val bundle = requireArguments()
        groupId = bundle.getString("groupId", "")
        groupName = bundle.getString("groupName", "")
        groupDescription = bundle.getString("groupDescription", "")

        groupNameEditText.setText(groupName)
        groupDescriptionEditText.setText(groupDescription)

        userListField = view.findViewById(R.id.userPreviewList)

        // Create an adapter for displaying added users with the new layout
        userListsAdapter = object : ArrayAdapter<User>(
            requireContext(),
            R.layout.simple_user_dropdown_item,  // This is the layout you provided
            addedUsers
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Inflate the custom layout
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.simple_user_dropdown_item, parent, false)

                val upperText = view.findViewById<TextView>(R.id.upperText)
                val deleteButton = view.findViewById<ImageView>(R.id.deleteButton)

                val user = getItem(position)
                upperText.text = user?.email

                // Set up the delete button to remove the user from the addedUsers list
                if (user?.userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    deleteButton.visibility = View.GONE
                } else {
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.setOnClickListener {
                        remove(user)
                        notifyDataSetChanged()
                    }
                }
                return view
            }
        }

        // Set the adapter for the ListView
        userListField.adapter = userListsAdapter

        if (groupId.isNotEmpty()) {
            loadGroupDataFromFirebase(groupId)
        } else {
            val currentUserEmail = auth.currentUser?.email
            if (!currentUserEmail.isNullOrEmpty() && addedUsers.none { it.email == currentUserEmail }) {
                val currentUserId = auth.currentUser?.uid ?: return view
                val currentUser = User(userId = currentUserId, email = currentUserEmail)
                addedUsers.add(0, currentUser)  // Add the logged-in user at the top
                userListsAdapter.notifyDataSetChanged()  // Refresh the ListView
            }
        }

        // Set up the suggestion field for user search (auto-complete)
        suggestionField = view.findViewById(R.id.userSuggestions)
        suggestionField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchUsers(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        suggestionField.setOnItemClickListener { _, _, position, _ ->
            val selectedUserEmail = suggestionField.adapter.getItem(position) as String

            // Query Firestore to get the userId associated with the selected email
            firestore.collection("users")
                .whereEqualTo("email", selectedUserEmail)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Retrieve the userId from the Firestore result
                    val document = result.documents.first()
                    val userId = document.id  // The userId is the document ID in Firestore

                    // Create a User object with the userId and email
                    val newUser = User(userId = userId, email = selectedUserEmail)

                    // Add the user to the addedUsers list if it is not already present
                    if (!addedUsers.contains(newUser)) {
                        addedUsers.add(newUser)
                        userListsAdapter.notifyDataSetChanged()  // Refresh the ListView
                    }

                    // Clear the text field after selection
                    suggestionField.text.clear()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching user: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            saveGroup()  // Call the function to save the group
        }

        return view
    }

    private fun loadGroupDataFromFirebase(groupId: String) {
        firestore.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""

                    groupNameEditText.setText(name)
                    groupDescriptionEditText.setText(description)

                    // Load members from Firestore and add them to addedUsers
                    val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()

                    // Clear existing users in addedUsers to prevent duplicates
                    addedUsers.clear()

                    // Loop through the members and add each to addedUsers list
                    members.forEach { member ->
                        val userId = member["userId"] as? String
                        val email = member["email"] as? String

                        if (userId != null && email != null) {
                            val user = User(userId = userId, email = email)
                            addedUsers.add(user)
                        }
                    }
                    userListsAdapter.notifyDataSetChanged()  // Refresh the ListView
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GroupEditorFragment", "Error loading group: ${exception.message}")
                Toast.makeText(requireContext(), "Error loading group: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchUsers(query: String) {
        firestore.collection("users")
            .orderBy("email")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                suggestions.clear()  // Clear previous search results
                if (result.isEmpty) {
                    Log.d("UserSuggestions", "No users found.")
                }
                // Add emails from the result to the suggestions
                for (document in result) {
                    val email = document.getString("email") ?: ""
                    suggestions.add(email)
                }
                // Update the adapter with new suggestions
                val suggestionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                suggestionField.setAdapter(suggestionAdapter)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching users: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGroup() {
        val name = groupNameEditText.text.toString().trim()
        val description = groupDescriptionEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (addedUsers.size < 2) {
            Toast.makeText(requireContext(), "Must have at least 2 members.", Toast.LENGTH_SHORT).show()
            return
        }

        val userID = auth.currentUser?.uid ?: return

        // Check if the groupId is empty. If it's empty, create a new group.
        val groupRef = if (groupId.isEmpty()) {
            // Create a new document with an auto-generated ID
            firestore.collection("groups").document()
        } else {
            // Use the provided groupId to reference an existing group
            firestore.collection("groups").document(groupId)
        }

        val groupData = hashMapOf(
            "name" to name,
            "description" to description,
            "admin" to userID,
            "members" to addedUsers // Save added users to the group
        )

        // Save the group data
        groupRef.set(groupData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Group successfully made!", Toast.LENGTH_SHORT).show()
                navigateToGroupView()
            }
            .addOnFailureListener { exception ->
                Log.e("GroupEditorFragment", "Error saving group: ${exception.message}")
                Toast.makeText(requireContext(), "Error saving group: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToGroupView() {
        findNavController().navigate(R.id.navigation_group)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }
}
