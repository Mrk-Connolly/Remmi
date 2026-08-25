package com.remmi.app.core.android.notifications.implementations

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.remmi.app.R
import com.remmi.app.core.android.notifications.NotificationService

/**
 * SYSTEM NOTIFICATION SERVICE
 * 
 * Android-specific implementation for posting system notifications.
 */
class SystemNotificationService(private val context: Context) : NotificationService {

    override fun postNotification(
        title: String,
        content: String,
        useSound: Boolean,
        useVibration: Boolean,
        tag: String?,
        ongoing: Boolean
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = if (ongoing) "remmi_summary" else "remmi_alarms"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = if (ongoing) "Remmi Summary" else "Remmi Alarms"
                val importance = if (ongoing) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
                
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    enableVibration(useVibration && !ongoing)
                    if (!useSound || ongoing) {
                        setSound(null, null)
                    }
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(if (ongoing) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
                .setOngoing(ongoing)
                .setAutoCancel(!ongoing)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if (!useSound || ongoing) builder.setSound(null)
            if (!useVibration || ongoing) builder.setVibrate(longArrayOf(0L))

            val notificationId = tag?.hashCode() ?: System.currentTimeMillis().toInt()
            notificationManager.notify(tag, notificationId, builder.build())
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to post notification: ${e.message}")
        }
    }
}
