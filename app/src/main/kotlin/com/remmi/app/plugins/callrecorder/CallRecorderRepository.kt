package com.remmi.app.plugins.callrecorder

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.callrecorder.models.CallRecording

class CallRecorderRepository : MemoryRepository<CallRecording>() {
    init {
        Log.d("Remmi", "[CallRecorderRepository] - Initialized")
    }
}
