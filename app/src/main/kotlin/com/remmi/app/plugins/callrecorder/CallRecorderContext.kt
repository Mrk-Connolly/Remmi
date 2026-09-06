package com.remmi.app.plugins.callrecorder

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object CallRecorderContext {
    var context: Context? = null
        set(value) {
            if (field == null) {
                field = value?.applicationContext
            }
        }
}
