package com.remmi.app.plugins.weather

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object WeatherContext {
    var context: Context? = null
        set(value) {
            if (field == null) {
                field = value?.applicationContext
            }
        }
}
