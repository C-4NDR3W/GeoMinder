package com.example.geominder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val profileNameTextView = view.findViewById<TextView>(R.id.profileName)
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)
        val noteCountTextView = view.findViewById<TextView>(R.id.noteCount)
        val noteActiveTextView = view.findViewById<TextView>(R.id.noteActive)
        val profileImageView = view.findViewById<ImageView>(R.id.profilePicture)
        val defaultProfilePicture = R.drawable.default_account_profile_foreground

        if (user != null) {
            val userId = user.uid
            val email = user.email
            emailTextView.text = email

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val profileName = document.getString("name") //please bikin user table on signup
                    profileNameTextView.text = profileName

                    var profilePictureUrl = document.getString("profilePicture")

                    for (profile in user.providerData) {
                        if (profile.providerId == "google.com") {
                            profilePictureUrl = profile.photoUrl?.toString()
                        }
                    }

                    Glide.with(this).load(profilePictureUrl ?: defaultProfilePicture)
                        .into(profileImageView)
                }
            }
                .addOnFailureListener { exception ->
                    Log.d("Accessing firestore Error", exception.toString())
                }

            db.collection("users").document(userId).collection("notes").get()
                .addOnSuccessListener { querySnapshot ->
                    val noteCount = querySnapshot.size()
                    noteCountTextView.text = noteCount.toString()
                }.addOnFailureListener { exception ->
                Log.d("Accessing firestore notes Error", exception.toString())
            }


            db.collection("users").document(userId).collection("notes")
                .whereEqualTo("status", "active").get().addOnSuccessListener { querySnapshot ->
                val noteActive = querySnapshot.size()
                noteCountTextView.text = noteActive.toString()
            }.addOnFailureListener { exception ->
                Log.d("Accessing firestore notes Error", exception.toString())
            }

        }


    }

    companion object {
    }
}