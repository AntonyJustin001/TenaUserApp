package tena.health.care.screens.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import tena.health.care.MainActivity
import tena.health.care.R
import tena.health.care.models.Notification
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received notification here
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        // Create the notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Your Channel Description"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.app_logo) // Set your custom notification icon here
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        storeNotification(
            Notification(
                notificationId = UUID.randomUUID().toString(),
                notificationTitle = "$title",
                notificationMessage = "$messageBody",
                receivedDate = current.format(formatter),
            ))

    }

    fun storeNotification(notification: Notification) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notifications")
            .document(notification.notificationId)
            .set(notification)
            .addOnSuccessListener {
                // Product successfully written!
                println("Notification added successfully")
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error adding notification: $e")
            }
    }
}

