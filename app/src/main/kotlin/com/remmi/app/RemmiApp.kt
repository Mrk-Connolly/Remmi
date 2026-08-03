package com.remmi.app

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.host.RemmiHost

@Composable
fun RemmiApp() {
    Log.d("Remmi", "Host launched")

    RemmiHost()
}