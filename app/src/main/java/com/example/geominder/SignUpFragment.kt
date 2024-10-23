package com.example.geominder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth



class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var confirmPassword : EditText

    private lateinit var redirectToLogin: TextView



    data object errors  {
        var emailError = false
        var passwordError = false
        var confirmError = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        emailEditText = view.findViewById(R.id.signup_emailEditText)
        passwordEditText = view.findViewById(R.id.signup_passwordEditText)
        confirmPassword = view.findViewById(R.id.confirmPass)
        signUpButton = view.findViewById(R.id.signUpButton)
        redirectToLogin = view.findViewById(R.id.signup_redirect)

        redirectToLogin.setOnClickListener( {
            redirectToLogin()
        })
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUpUser(email, password)
            }

            else {
                Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePass(password: String): Boolean {
        if (password.length <= 8) {
            return false
        }
        val specialCharacters = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~"
        var hasSpecialCharacter = false
        for (char in password) {
            if (char in specialCharacters) {
                hasSpecialCharacter = true
                break
            }
        }
        return hasSpecialCharacter
    }

    private fun comparePass(password: String, confirm: String): Boolean {
        if (password != confirm) {
            return false
        }

        return true
    }

    private fun validateEmail(email: String) : Boolean {
        if (!email.contains("@") || !email.contains(".")) {
            return false
        }
        val parts = email.split("@")
        val address = parts[0]
        val domain = parts[1]
        if (address.isEmpty() ||domain.split(".").size < 2) {
            return false
        }
        return true
    }

    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Sign-up successful", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(context, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            .addOnFailureListener({
                Toast.makeText(context, "Sign-up failed: ${it.message}", Toast.LENGTH_SHORT).show()
            })
    }

    private fun redirectToLogin() {
        findNavController().navigate(R.id.action_signUpFragment2_to_loginFragment)
    }
}
