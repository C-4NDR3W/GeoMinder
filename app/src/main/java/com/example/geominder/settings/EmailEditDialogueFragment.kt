package com.example.geominder.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.geominder.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class EmailEditDialogueFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_edit_dialogue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newEmailEditText = view.findViewById<EditText>(R.id.currentEmail)
        val passwordEditText = view.findViewById<EditText>(R.id.emailConfirmPassword)
        val changeEmailButton = view.findViewById<Button>(R.id.changeEmailButton)

        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val user = FirebaseAuth.getInstance().currentUser

            if (validateInputs(newEmail, password)) {
                reauthenticateUser(password) { isAuthenticated ->
                    if (isAuthenticated) {
                        updateEmail(newEmail)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun validateInputs(newEmail: String, password: String): Boolean {
        if (newEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun reauthenticateUser(password: String, callback: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
        }
    }

    private fun updateEmail(newEmail: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Verification email sent to $newEmail. Please verify to complete the change.",
                    Toast.LENGTH_LONG
                ).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Failed to update email.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}