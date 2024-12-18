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
import com.google.gson.Gson
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GroupEditorFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var suggestionField: AutoCompleteTextView
    private lateinit var userListField: ListView
    private lateinit var actionButton: Button
    private lateinit var groupNameField: EditText
    private lateinit var groupDescField: EditText

    private var isEditing = false

    private var addedUsers = mutableListOf<String>()
    private var suggestions = mutableListOf<String>()

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var userListsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_group_editor, container, false)

        context?.let {
            adapter = ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, suggestions)
        }

        actionButton = view.findViewById(R.id.groupActionButton)
        groupNameField = view.findViewById(R.id.groupNameField)
        groupDescField = view.findViewById(R.id.groupDescField)

        val members = arguments?.getString("groupName")

        if (arguments?.getString("members") != null) {
            Log.d("edit", "CURRENTLY EDITING")
            isEditing = true
            val groupName = arguments?.getString("groupName")
            val groupDesc = arguments?.getString("groupDesc")
            val membersJson = arguments?.getString("members")

            addedUsers = populateMembersWithEmails(membersJson)
            actionButton.text = "Edit Group"

            groupNameField.setText(groupName)
            groupDescField.setText(groupDesc)
        }

        userListField = view.findViewById(R.id.userPreviewList)
        userListsAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_user_dropdown_item,
            addedUsers
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.simple_user_dropdown_item, parent, false)

                val upperText = view.findViewById<TextView>(R.id.upperText)
                val deleteButton = view.findViewById<ImageView>(R.id.deleteButton)

                val user = getItem(position)
                upperText.text = user

                deleteButton.setOnClickListener {
                    Toast.makeText(context, "$user deleted", Toast.LENGTH_SHORT).show()
                    addedUsers.remove(user)
                    notifyDataSetChanged()
                }
                return view
            }
        }
        userListField.adapter = userListsAdapter

        suggestionField = view.findViewById(R.id.userSuggestions)
        suggestionField.setAdapter(adapter)
        suggestionField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                generateUserSuggestions(s.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        setActionButtonListener(isEditing)
        suggestionField.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String

            if (selectedUser in addedUsers) {
                Toast.makeText(context, "$selectedUser is already added!", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            Log.d("User", "Selected user: $selectedUser")
            addedUsers.add(selectedUser)
            userListsAdapter.notifyDataSetChanged()
            suggestionField.text.clear()
            suggestions.remove(selectedUser)
            adapter.notifyDataSetChanged()
        }

        return view
    }

    private fun populateMembersWithEmails(membersJson: String?): MutableList<String> {
        if (membersJson == null) {
            return mutableListOf()
        }

        val gson = Gson()
        val listType = object : TypeToken<MutableList<User>>() {}.type
        val members = gson.fromJson<MutableList<User>>(membersJson, listType)
        return members.map { it.email }.toMutableList()
    }

    private fun validateGroupContent(): Boolean {
        return groupNameField.text.isNotEmpty() && addedUsers.size >= 2
    }

    private fun setActionButtonListener(isEditing: Boolean) {
        actionButton.setOnClickListener {
            val name = groupNameField.text.toString()
            val desc = groupDescField.text.toString()

            if (!validateGroupContent()) {
                Toast.makeText(context, "Group must have a name and at least two members!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val groupId = arguments?.getString("groupId")
            Log.d("GroupEditorFragment", "groupId: $groupId")

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (auth.currentUser == null) {
                        Toast.makeText(context, "User not authenticated.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    if (isEditing && groupId != null) {
                        editGroup(addedUsers, groupId, auth.currentUser!!.uid, name, desc)
                        Toast.makeText(context, "Group edited successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        createGroup(addedUsers, auth.currentUser!!.uid, name, desc)
                        Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    }

                    navigateToGroupListFragment()
                } catch (e: Exception) {
                    Log.e("GroupError", "Failed to create/edit group", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateUserSuggestions(query: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("email", query)
            .whereLessThan("email", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val newSuggestions = mutableListOf<String>()
                for (document in documents) {
                    val email = document.getString("email")
                    Log.d("User", "Matching email: $email")

                    if (email != null && email !in addedUsers && email !in suggestions) {
                        newSuggestions.add(email)
                    }
                }

                suggestions.clear()
                suggestions.addAll(newSuggestions)
                adapter.clear()
                adapter.addAll(suggestions)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error fetching documents", e)
            }
    }

    private suspend fun obtainUserIds(users: List<String>): MutableList<String> {
        val userIds = mutableListOf<String>()
        for (user in users) {
            val snapshot = db.collection("users")
                .whereEqualTo("email", user)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val documentId = snapshot.documents[0].id
                userIds.add(documentId)
            }
        }
        return userIds
    }

    private suspend fun editGroup(
        users: List<String>,
        groupId: String,
        adminId: String,
        groupName: String,
        groupDesc: String
    ) {
        if (!validateGroupContent()) {
            Toast.makeText(requireContext(), "Group must have at least two members!", Toast.LENGTH_SHORT).show()
            return
        }

        val userObjects = obtainUserIds(users).mapIndexed { index, userId ->
            hashMapOf("email" to users[index], "userId" to userId)
        }

        val groupData = hashMapOf(
            "name" to groupName,
            "admin" to adminId,
            "desc" to groupDesc,
            "members" to userObjects
        )

        db.collection("groups").document(groupId).update(groupData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Group updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error updating group", exception)
                Toast.makeText(requireContext(), "Failed to update group", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun createGroup(users: List<String>, adminId: String, groupName: String, groupDesc: String) {
        if (!validateGroupContent()) {
            Toast.makeText(requireContext(), "Group must have at least two members!", Toast.LENGTH_SHORT).show()
            return
        }

        val userObjects = obtainUserIds(users).mapIndexed { index, userId ->
            hashMapOf("email" to users[index], "userId" to userId)
        }

        val groupData = hashMapOf(
            "name" to groupName,
            "admin" to adminId,
            "desc" to groupDesc,
            "members" to userObjects
        )

        db.collection("groups").add(groupData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), "Group created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error creating group", exception)
                Toast.makeText(requireContext(), "Failed to create group", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToGroupListFragment() {
        findNavController().navigate(R.id.action_groupEditorFragment_to_groupListFragment)
    }
}
