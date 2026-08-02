package com.remmi.app.core.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.remmi.app.HomeScreen
import com.remmi.app.core.navigation.AppNavigation
import com.remmi.app.core.runtime.RemmiRuntime

@Composable
fun RemmiHost() {

    val runtime = remember {
        RemmiRuntime()
    }

    LaunchedEffect(Unit) {
        runtime.start()
    }

    AppNavigation()
}
