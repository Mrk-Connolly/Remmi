package com.remmi.app.core.host

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.remmi.app.core.navigation.AppNavigation
import com.remmi.app.core.runtime.RemmiRuntime
import com.remmi.app.core.widgets.WidgetManager

@Composable
fun RemmiHost() {

    Log.d("Remmi", "Runtime generated")

    val runtime = remember {
        RemmiRuntime().apply {
            Log.d("Remmi", "Runtime executed")
            start()
        }
    }



    AppNavigation(widgetManager = runtime.widgetManager)
}
