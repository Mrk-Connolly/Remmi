package com.remmi.app.plugins.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Remmi", "[AlarmReceiver] - [onReceive] executed")
        val action = intent.action
        Log.d("AlarmReceiver", "onReceive triggered. Action: $action")
        
        val alarmId = intent.getStringExtra("ALARM_ID")
        val alarmTitle = intent.getStringExtra("ALARM_TITLE") ?: "Alarm"
        
        Log.d("AlarmReceiver", "Processing broadcast for ID: $alarmId, Title: $alarmTitle")
        
        if (alarmId == null) {
            Log.e("AlarmReceiver", "Abort onReceive: Missing ALARM_ID extra.")
            return
        }
        
        val handler = AndroidAlarmHandler(context)
        handler.postNotification(alarmTitle, "Your alarm is ringing!")
        Log.d("AlarmReceiver", "Notification posted for alarm: $alarmId")
    }
}
