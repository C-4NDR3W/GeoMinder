package com.example.geominder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.VibrationEffect
import android.os.Vibrator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams) {

    private val applicationContext = context

    override fun doWork(): Result {
        val noteTitle = inputData.getString("noteTitle") ?: "Reminder"
        val noteId = inputData.getString("noteId") ?: "Unknown"

        saveNotificationToFirestore(noteTitle, noteId) // Simpan notifikasi ke Firestore
        triggerNotification(noteTitle)
        return Result.success()
    }


    private fun triggerNotification(noteTitle: String) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel("reminder_channel_id")
        } else {
            ""
        }

        // Buat Intent untuk membuka aplikasi
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.app.PendingIntent.FLAG_IMMUTABLE
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Bangun notifikasi dengan PendingIntent
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Geominder")
            .setContentText("Your event is starting soon!")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set PendingIntent di sini
            .setAutoCancel(true) // Notifikasi akan otomatis hilang saat diklik
            .build()

        // Tampilkan notifikasi
        notificationManager.notify(1, notification)

        // Get system Vibrator service and vibrate the phone
        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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


