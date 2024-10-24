package com.example.geominder

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        getEmailPassword()
        getNoteCountStatus()

        val editNameButton = view.findViewById<ImageView>(R.id.editNameIcon)
        val editEmailButton = view.findViewById<ImageView>(R.id.emailEditButton)
        val editPasswordButton = view.findViewById<ImageView>(R.id.passwordEditButton)
        val editProfilePictureButton = view.findViewById<ImageView>(R.id.profileEditButton)

        editNameButton.setOnClickListener{
            handleEditName()
        }

        editEmailButton.setOnClickListener{
            handleEditEmail()
        }

        editPasswordButton.setOnClickListener{
            handleEditPassword()
        }

        editProfilePictureButton.setOnClickListener{
            if(checkCameraPermission() && checkGalleryPermission()){
                handleEditProfilePicture()
            } else{
                requestCameraPermission()
                requestGalleryPermission()
            }
        }

    }

    private fun getEmailPassword() {
        val profileNameTextView = view?.findViewById<TextView>(R.id.profileName)
        val emailTextView = view?.findViewById<TextView>(R.id.emailTextView)

        val profileImageView = view?.findViewById<ImageView>(R.id.profilePicture)
        val defaultProfilePicture = R.drawable.default_account_profile_foreground

        if (user != null) {
            val userId = user.uid

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val profileName = document.getString("name")
                    profileNameTextView?.text = profileName

                    var profilePictureUrl = document.getString("profilePicture")

                    for (profile in user.providerData) {
                        if (profile.providerId == "google.com") {
                            profilePictureUrl = profile.photoUrl?.toString()
                        }
                    }

                    if (profileImageView != null) {
                        Glide.with(this).load(profilePictureUrl ?: defaultProfilePicture)
                            .into(profileImageView)
                    }
                }
            }
                .addOnFailureListener { exception ->
                    Log.d("Accessing firestore Error", exception.toString())
                }
        }
    }

    private fun getNoteCountStatus() {
        if (user != null) {
            val userId = user.uid

            val noteCountTextView = view?.findViewById<TextView>(R.id.noteCount)
            val noteActiveTextView = view?.findViewById<TextView>(R.id.noteActive)
            db.collection("users").document(userId).collection("notes").get()
                .addOnSuccessListener { querySnapshot ->
                    val noteCount = querySnapshot.size()
                    noteCountTextView?.text = noteCount.toString()
                }.addOnFailureListener { exception ->
                    Log.d("Accessing firestore notes Error", exception.toString())
                }


            db.collection("users").document(userId).collection("notes")
                .whereEqualTo("status", "true").get().addOnSuccessListener { querySnapshot ->
                    val noteActive = querySnapshot.size()
                    noteActiveTextView?.text = noteActive.toString()
                }.addOnFailureListener { exception ->
                    Log.d("Accessing firestore notes Error", exception.toString())
                }

        }
    }

    private fun handleEditName(){
        val profileNameTextView = view?.findViewById<TextView>(R.id.profileName)
        val profileNameEditText = EditText(requireContext())
        profileNameEditText.setText(profileNameTextView?.text.toString())

        val parent = profileNameTextView?.parent as ViewGroup
        val index = parent.indexOfChild(profileNameTextView)
        parent.removeView(profileNameTextView)
        parent.addView(profileNameEditText, index)

        profileNameEditText.setOnFocusChangeListener{ v, hasFocus ->
            if(!hasFocus){
                val newProfileName = profileNameEditText.text.toString()

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null){
                    FirebaseFirestore.getInstance().collection("users").document(userId).update("name", newProfileName)
                        .addOnSuccessListener {
                            profileNameTextView.text = newProfileName
                            parent.removeView(profileNameEditText)
                            parent.addView(profileNameTextView, index)
                        }
                        .addOnFailureListener{
                            Toast.makeText(requireContext(), "An error has occured while updating", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun handleEditEmail(){
        val dialog = EmailEditDialogueFragment()
        dialog.show(parentFragmentManager, "EmailEditDialogueFragment")

    }

    private fun handleEditPassword(){
        val dialog = PasswordEditDialogFragment()
        dialog.show(parentFragmentManager, "PasswordEditDialogueFragment")
    }

    private fun handleEditProfilePicture(){
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext()).setTitle("Edit Profile Picture").setItems(options){ dialog, which ->
            when(which){
                0 -> openCamera()
                1 -> openGallery()
            }
        }.show()
    }

    private fun openCamera(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, MEDIA_IMAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGalleryPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission(){ //reminder to check out the non deprecated version
        requestPermissions(
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestGalleryPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                MEDIA_IMAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                MEDIA_IMAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

//    make on activityResult or find non-deprecated version

    companion object{
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
        const val MEDIA_IMAGE_REQUEST_CODE = 1003
        const val MEDIA_IMAGE_PERMISSION_REQUEST_CODE = 1004 //cheonsa
    }

}