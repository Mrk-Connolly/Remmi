package com.remmi.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import com.remmi.app.core.host.RemmiHost
import com.remmi.app.core.screens.RemmiApp

class MainActivity : ComponentActivity() {
    // Android launches this class when the user opens Remmi.

    private lateinit var remmiHost: RemmiHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Remmi system
        remmiHost = RemmiHost(applicationContext)
        remmiHost.start()

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Log.d("Remmi", "Remmi app UI started")
                    RemmiApp(remmiHost)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        remmiHost.stop()
    }
}
