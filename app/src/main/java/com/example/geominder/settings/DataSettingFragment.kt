package com.example.geominder.settings

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.geominder.LoginFragment
import com.example.geominder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DataSettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.data_preferences, rootKey)
        setupExportDataPreference()
        setupDeleteDataPreference()
    }

    private fun setupExportDataPreference() {
        val exportDataPreference = findPreference<Preference>("data_export")
        exportDataPreference?.setOnPreferenceClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                fetchUserNotesFromFirebase(userId) { notes ->
                    val fileName = "notes_backup_${System.currentTimeMillis()}.txt"
                    val fileContent = notes.joinToString("\n")
                    saveFileToDocuments(fileName, fileContent)
                }
            } else {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun setupDeleteDataPreference() {
        val deleteDataPreference = findPreference<Preference>("delete_data")
        deleteDataPreference?.setOnPreferenceClickListener {
            showDeleteAccountDialog()
            true
        }
    }

    private fun saveFileToDocuments(fileName: String, fileContent: String) {
        val resolver = requireContext().contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(fileContent.toByteArray())
                Toast.makeText(context, "File saved to Documents folder", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Failed to open output stream", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account and Data")
            .setMessage("Are you sure you want to permanently delete your account and all notes? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccountAndData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccountAndData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Account and data deleted", Toast.LENGTH_SHORT)
                            .show()
                        navigateToLoginScreen()
                    } else {
                        Toast.makeText(
                            context,
                            "Error deleting account: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting data: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(context, LoginFragment::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun fetchUserNotesFromFirebase(userId: String, onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val notesRef = db.collection("users").document(userId).collection("notes")

        notesRef.get()
            .addOnSuccessListener { querySnapshot ->
                val notes = querySnapshot.documents.mapNotNull { it.getString("noteContent") }
                onResult(notes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching notes: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                onResult(emptyList())
            }
    }
}