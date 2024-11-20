package com.example.geominder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField


class GroupEditorFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var suggestionField: AutoCompleteTextView
    private lateinit var userListField: ListView
    private lateinit var fragmentTitleText : TextView

    private var addedUsers = mutableListOf<String>("kevinhadinata11@gmail.com", "kennylukman@gmail.com")
    private var suggestions = mutableListOf<String>("kevinhadinata11@gmail.com", "kennylukman@gmail.com")

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var userListsAdapter : ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }


    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_group_editor, container, false)
        context?.let {
            adapter = ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, suggestions)
//            userListsAdapter = ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, addedUsers)
        }

        fragmentTitleText = view.findViewById(R.id.newGroupText)

        //jika fragment dipakai untuk mengedit group
        if (arguments?.getStringArrayList("users") != null)
        {
            addedUsers = arguments?.getStringArrayList("users") as MutableList<String>
            fragmentTitleText.text = "Edit Group"

        }

        userListField = view.findViewById(R.id.userPreviewList)
        userListsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, addedUsers)
        userListField.adapter = userListsAdapter

        suggestionField = view.findViewById(R.id.userSuggestions)
        suggestionField.setAdapter(adapter)
        suggestionField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // gk bisa di delete
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                generateUserSuggestions(s.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {
                // gk bisa di delete
            }
        })


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

    fun generateUserSuggestions(query: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("email", query)
            .whereLessThan("email", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val newSuggestions = mutableListOf<String>()
                for (document in documents) {
                    val email = document.getString("email")
                    Log.d("User", "Matching email: $email")

                    if (email == null || email in addedUsers || email in suggestions) {
                        continue
                    }
                    newSuggestions.add(email)
                }

                suggestions.addAll(newSuggestions)
                adapter.clear()
                adapter.addAll(suggestions)
                adapter.notifyDataSetChanged()



            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error fetching documents", e)
            }
    }
//


}
