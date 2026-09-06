package com.remmi.app.plugins.callrecorder.logic

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.remmi.app.plugins.callrecorder.models.CallDirection
import com.remmi.app.plugins.callrecorder.CallRecorderPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsAppAccessibilityService : AccessibilityService() {

    private var isCallActive = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "com.whatsapp") return

        val rootNode = rootInActiveWindow ?: return
        
        // WhatsApp call screen detection
        // Note: These strings/IDs are subject to change by WhatsApp
        val isWhatsAppCallScreen = findNodeByText(rootNode, "WhatsApp Call") != null ||
                                  findNodeByText(rootNode, "End Call") != null

        if (isWhatsAppCallScreen && !isCallActive) {
            startWhatsAppRecording(rootNode)
        } else if (!isWhatsAppCallScreen && isCallActive) {
            stopWhatsAppRecording()
        }
    }

    private fun startWhatsAppRecording(rootNode: AccessibilityNodeInfo) {
        isCallActive = true
        val contactName = findContactName(rootNode)
        val callInfo = CallInfo(
            phoneNumber = contactName, 
            direction = CallDirection.INCOMING, 
            appPackage = "com.whatsapp"
        )
        scope.launch {
            CallRecorderPlugin.getManager().startRecording(callInfo)
        }
    }

    private fun stopWhatsAppRecording() {
        isCallActive = false
        scope.launch {
            val recording = CallRecorderPlugin.getManager().stopRecording()
            recording?.let {
                CallRecorderPlugin.getActions().saveRecording(it)
            }
        }
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val list = node.findAccessibilityNodeInfosByText(text)
        return list?.firstOrNull()
    }

    private fun findContactName(node: AccessibilityNodeInfo): String? {
        // Logic to traverse nodes and find the contact name label
        // Simplified for this example
        return null
    }

    override fun onInterrupt() {}
}
