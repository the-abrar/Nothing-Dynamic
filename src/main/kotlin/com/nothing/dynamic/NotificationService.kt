package com.nothing.dynamic

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent

/**
 * NotificationService extends the system NotificationListenerService to listen for
 * incoming notifications, parse their titles and text bodies, and broadcast
 * the information to other components in the same process.
 */
class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        val packageName = sbn.packageName

        val notification: Notification = sbn.notification
        val title = notification.extras.getString(Notification.EXTRA_TITLE)
        val text = notification.extras.getString(Notification.EXTRA_TEXT)

        // Build a local intent and broadcast the data
        val intent = Intent("com.nothing.dynamic.UPDATE_STATUS")
        intent.putExtra("PACKAGE", packageName)
        intent.putExtra("TITLE", title)
        intent.putExtra("TEXT", text)
        sendBroadcast(intent)
    }
}
