package com.remmi.app.plugins.callrecorder.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.remmi.app.plugins.callrecorder.models.CallDirection

class CallStateMonitor(
    private val onCallStarted: (CallInfo) -> Unit,
    private val onCallEnded: () -> Unit
) : BroadcastReceiver() {

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var isRecording = false
    private var currentNumber: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            currentNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            return
        }

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        val state = when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            else -> return
        }

        handleStateChange(state, incomingNumber)
    }

    private fun handleStateChange(state: Int, incomingNumber: String?) {
        if (state == lastState) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Incoming call start ringing
                currentNumber = incomingNumber
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call answered or outgoing call started
                if (lastState == TelephonyManager.CALL_STATE_IDLE) {
                    // Outgoing call (IDLE -> OFFHOOK)
                    onCallStarted(CallInfo(currentNumber, CallDirection.OUTGOING))
                } else if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // Incoming call answered (RINGING -> OFFHOOK)
                    onCallStarted(CallInfo(currentNumber, CallDirection.INCOMING))
                }
                isRecording = true
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                if (isRecording) {
                    onCallEnded()
                    isRecording = false
                }
                currentNumber = null
            }
        }
        lastState = state
    }
}
