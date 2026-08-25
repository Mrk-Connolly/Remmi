package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.model.alarm.AlarmItem

/**
 * Wrapper for AlarmItem to handle UI state like whether it's a local system alarm.
 */
data class AlarmUiModel(
    val alarm: AlarmItem,
    val isLocal: Boolean
) {
    init {
        Log.d("Remmi", "[AlarmUiModel] - [constructor] executed")
    }
}
