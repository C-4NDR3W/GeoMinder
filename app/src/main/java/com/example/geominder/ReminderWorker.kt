package com.example.geominder

import android.content.Context
import android.location.Location
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.VibrationEffect
import android.os.Vibrator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val applicationContext = context

    override fun doWork(): Result {
        val noteTitle = inputData.getString("noteTitle") ?: "Reminder"
        val noteId = inputData.getString("noteId") ?: "Unknown"

        saveNotificationToFirestore(noteTitle, noteId) // Simpan notifikasi ke Firestore
        triggerNotification(noteTitle)
        fetchCoordinatesAndCheckProximity(noteId)
        return Result.success()
    }

    private fun fetchCoordinatesAndCheckProximity(noteId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notes")
            .document(noteId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val latitude = document.getDouble("latitude") ?: return@addOnSuccessListener
                    val longitude = document.getDouble("longitude") ?: return@addOnSuccessListener

                    checkProximity(latitude, longitude)
                } else {
                    Log.d("ReminderWorker", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ReminderWorker", "Error fetching coordinates", exception)
            }
    }

    private fun checkProximity(savedLatitude: Double, savedLongitude: Double) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLocation = Location("currentLocation").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }

                val savedLocation = Location("savedLocation").apply {
                    latitude = savedLatitude
                    longitude = savedLongitude
                }

                val distanceInMeters = currentLocation.distanceTo(savedLocation)

                if (distanceInMeters <= 50) {
                    triggerNotification("You are within 50 meters of your saved location!")
                }
            } else {
                Log.d("ReminderWorker", "No location found")
            }
        }
    }

    private fun triggerNotification(noteTitle: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val enableVibration = sharedPreferences.getBoolean("enableVibration", true)
        val vibrationMode = sharedPreferences.getString("vibrationMode", "default") ?: "default"
        val vibrationLengthKey = sharedPreferences.getString("vibrationLength", "long") ?: "long"

        val vibrationLength = when (vibrationLengthKey) {
            "short" -> 500L
            "medium" -> 1000L
            "long" -> 2000L
            else -> 2000L
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("reminder_channel_id")
        } else {
            ""
        }

        // Create Intent to open the app
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.PendingIntent.FLAG_IMMUTABLE
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Build notification with PendingIntent
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Geominder") // Notification title
            .setContentText(noteTitle)   // Notification content
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set PendingIntent here
            .setAutoCancel(true) // Notification will disappear when clicked
            .build()

        // Show notification
        notificationManager.notify(1, notification)

        // Get system Vibrator service and vibrate the phone
        if (enableVibration) {
            val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val vibrationEffect = when (vibrationMode) {
                    "tick" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    "heavy_click" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    "click" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    else -> VibrationEffect.createOneShot(vibrationLength, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(vibrationEffect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationLength, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(vibrationLength)
            }
        }
    }

    private fun saveNotificationToFirestore(noteTitle: String, noteId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationData = hashMapOf(
            "title" to noteTitle,
            "noteId" to noteId,
            "dateTime" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d("ReminderWorker", "Notification saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("ReminderWorker", "Error saving notification", exception)
            }
    }

    private fun createNotificationChannel(channelId: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val descriptionText = "Channel for event reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }
}