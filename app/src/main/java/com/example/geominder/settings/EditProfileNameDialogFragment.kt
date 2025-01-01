package com.example.geominder.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.geominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileNameDialogFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    interface OnNameChangeListener { //for notifying ProfileFragment
        fun onNameChanged()
    }

    private var listener: OnNameChangeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as? OnNameChangeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnNameChangeListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile_name_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newNameEditText = view.findViewById<EditText>(R.id.newProfileName)
        val changeNameButton = view.findViewById<Button>(R.id.changeNameButton)

        changeNameButton.setOnClickListener {
            val newName = newNameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid

                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(userId).update("name", newName)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Name Updated", Toast.LENGTH_SHORT)
                                .show()
                            listener?.onNameChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Unexpected Error Occurred, please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "Name field cannot be empty.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}