package com.remmi.app.plugins.callrecorder.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallStateMonitor(
    private val onCallStarted: (String?) -> Unit,
    private val onCallEnded: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        
        Log.d("Remmi", "[CallStateMonitor] - State changed: $state, Number: $number")

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                onCallStarted(number)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                onCallEnded()
            }
        }
    }
}
