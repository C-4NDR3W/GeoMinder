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
import com.google.firebase.firestore.FirebaseFirestore


class GroupEditorFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var suggestionField: AutoCompleteTextView
    private var addedUsers = mutableListOf<String>()
    private var suggestions = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

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
        }

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

        return view
    }

    fun generateUserSuggestions(query: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("email", query)
            .whereLessThan("email", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val email = document.getString("email")
                    if (email in addedUsers || email == null) {
                        return@addOnSuccessListener
                    }
                    suggestions.add(email)
                    Log.d("User", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error fetching documents", e)
            }
    }
}
