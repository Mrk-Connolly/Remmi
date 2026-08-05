package com.remmi.app

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.host.RemmiHost

@Composable
fun RemmiApp() {
    // Remmi application starts by creating a host
    // The root Compose function
    Log.d("Remmi", "Host launched")
    RemmiHost()
}