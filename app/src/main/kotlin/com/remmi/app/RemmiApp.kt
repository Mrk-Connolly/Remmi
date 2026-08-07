package com.remmi.app

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.host.RemmiHost

/**
 * Remmi App, Fist step is to launch teh host
 */
@Composable
fun RemmiApp() {
    Log.d("Remmi", "Host launched")

    RemmiHost()
}