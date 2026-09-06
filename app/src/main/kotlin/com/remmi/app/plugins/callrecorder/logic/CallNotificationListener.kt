package com.remmi.app.plugins.callrecorder.logic

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.remmi.app.plugins.callrecorder.CallRecorderPlugin
import com.remmi.app.plugins.callrecorder.models.CallDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isWhatsAppCallActive = false

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp") return
        
        val notification = sbn.notification
        val category = notification.category
        
        // Detect WhatsApp call notification
        if (category == "call") {
            if (!isWhatsAppCallActive) {
                Log.d("Remmi", "[CallNotificationListener] - WhatsApp call detected via notification")
                isWhatsAppCallActive = true
                val callInfo = CallInfo(
                    phoneNumber = notification.extras.getString("android.title"),
                    direction = CallDirection.INCOMING,
                    appPackage = "com.whatsapp"
                )
                scope.launch {
                    CallRecorderPlugin.getManager().startRecording(callInfo)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp") return
        
        val notification = sbn.notification
        if (notification.category == "call" && isWhatsAppCallActive) {
            Log.d("Remmi", "[CallNotificationListener] - WhatsApp call notification removed")
            isWhatsAppCallActive = false
            scope.launch {
                val recording = CallRecorderPlugin.getManager().stopRecording()
                recording?.let {
                    CallRecorderPlugin.getActions().saveRecording(it)
                }
            }
        }
    }
}
