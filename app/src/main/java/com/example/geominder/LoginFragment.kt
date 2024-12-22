package com.example.geominder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment()
{

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var googleSignInBtn : SignInButton
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    fun setGoogleLogInButton(view: View)
    {
        googleSignInBtn = view.findViewById(R.id.googleSignInButton)
        googleSignInBtn.setOnClickListener {
            signInWithGoogle()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val loginButton = view.findViewById<Button>(R.id.loginSubmit)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.passwordEditText)


        setGoogleLogInButton(view)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        val signUpRedirect = view.findViewById<TextView>(R.id.redirect_signup)
        signUpRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment2)
        }

        return view
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)

                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()

            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true

                    val userObject = hashMapOf(
                        "uid" to user?.uid,
                        "email" to user?.email,
                    )

                    if (isNewUser) {

                        user?.uid?.let { uid ->
                            db.collection("users").document(uid)
                                .set(userObject)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "User added successfully")
                                    Toast.makeText(requireContext(), "Welcome, new user!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error adding user: ${e.message}")
                                    Toast.makeText(requireContext(), "Error adding user to database", Toast.LENGTH_SHORT).show()
                                }
                        }

                    }

                    Toast.makeText(requireContext(), "Google sign-in successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Google Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}
