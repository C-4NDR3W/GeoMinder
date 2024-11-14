package com.example.geominder

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.geominder.LoginFragment.Companion.RC_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var confirmPassword : EditText

    private lateinit var emailErrorText : TextView;
    private lateinit var pwdErrorText : TextView;
    private lateinit var confirmErrorText : TextView;

    private lateinit var redirectToLogin: TextView
    private lateinit var googleSignUpBtn : SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient



    data object errors {
        var emailError = false
        var passwordError = false
        var confirmError = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    fun setGoogleSignUpButton(view: View)
    {
        googleSignUpBtn = view.findViewById(R.id.googleSignUpButton)
        googleSignUpBtn.setOnClickListener {
            signUpWithGoogle()

        }
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
                    Toast.makeText(requireContext(), "Google sign-in successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Google Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    fun updateErrorIndicators() {
        emailErrorText.visibility = if (errors.emailError) View.VISIBLE else View.GONE
        pwdErrorText.visibility = if (errors.passwordError) View.VISIBLE else View.GONE
        confirmErrorText.visibility = if (errors.confirmError) View.VISIBLE else View.GONE
    }

    fun resetErrors()
    {
        errors.emailError = false
        errors.passwordError = false
        errors.confirmError = false
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        emailEditText = view.findViewById(R.id.signup_emailEditText)
        passwordEditText = view.findViewById(R.id.signup_passwordEditText)
        confirmPassword = view.findViewById(R.id.confirmPass)
        signUpButton = view.findViewById(R.id.signUpButton)
        redirectToLogin = view.findViewById(R.id.signup_redirect)

        emailErrorText = view.findViewById(R.id.emailValidationWarning)
        pwdErrorText = view.findViewById(R.id.pwdValidationWarning)
        confirmErrorText = view.findViewById(R.id.confirmValidationWarning)

        setGoogleSignUpButton(view)
        redirectToLogin.setOnClickListener {
            redirectToLogin()
        }


        signUpButton.setOnClickListener {
            resetErrors()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty()
                && validateEmail(email) && validatePass(password) && comparePass(password, confirm)) {
                signUpUser(email, password)
            }
//
            else {
                updateErrorIndicators()
            }
        }
    }

    private fun validatePass(password: String): Boolean {
        errors.passwordError = true
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



        errors.passwordError = hasSpecialCharacter
        return hasSpecialCharacter
    }

    private fun comparePass(password: String, confirm: String): Boolean {
        if (password != confirm) {
            errors.confirmError = true
            return false
        }

        return true
    }

    private fun validateEmail(email: String) : Boolean {
        if (!email.contains("@") || !email.contains(".")) {
            errors.emailError = true;
            return false
        }
        val parts = email.split("@")
        val address = parts[0]
        val domain = parts[1]
        if (address.isEmpty() ||domain.split(".").size < 2) {
            errors.emailError = true;
            return false
        }
        return true
    }

    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Sign-up successful", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                } else {
                    Toast.makeText(context, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            .addOnFailureListener{
                Toast.makeText(context, "Sign-up failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirectToLogin() {
        findNavController().navigate(R.id.action_signUpFragment2_to_loginFragment)
    }
}
