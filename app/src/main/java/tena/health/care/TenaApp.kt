package tena.health.care

import android.app.Application
import android.content.Intent
import com.onesignal.OneSignal

class TenaApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize OneSignal with the context and app ID
        OneSignal.initWithContext(this)
        OneSignal.setAppId("d973d0fa-e459-47f7-b591-d297636693e3")

        // Optionally, set logging level to debug for troubleshooting
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // Set notification opened handler
        OneSignal.setNotificationOpenedHandler { result ->
            val actionId = result.action.actionId
            val data = result.notification.additionalData

            // Handle the notification click action here
            // Example: Open a specific activity when a notification is clicked
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        // Set notification received handler (optional)
        OneSignal.setNotificationWillShowInForegroundHandler { notificationReceivedEvent ->
            val notification = notificationReceivedEvent.notification

            // You can access the notification data and decide to display it or handle it custom
            notificationReceivedEvent.complete(notification) // Show the notification in the foreground
        }

    }
}