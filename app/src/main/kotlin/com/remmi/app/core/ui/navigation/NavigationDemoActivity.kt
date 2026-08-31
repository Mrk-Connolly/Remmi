package com.remmi.app.core.ui.navigation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.remmi.app.R

class NavigationDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_navigation_demo)

        val navigation = findViewById<CurvedBottomNavigationView>(R.id.bottomNavigation)

        navigation.setItems(
            listOf(
                NavigationItem(R.drawable.ic_nav_home, "Home"),
                NavigationItem(R.drawable.ic_nav_activity, "Activity"),
                NavigationItem(R.drawable.ic_nav_wallet, "Wallet"),
                NavigationItem(R.drawable.ic_nav_profile, "Profile")
            )
        )

        navigation.setOnItemSelectedListener { index ->
            val labels = listOf("Home", "Activity", "Wallet", "Profile")
            Toast.makeText(this, "Selected: ${labels[index]}", Toast.LENGTH_SHORT).show()
        }

        navigation.setOnCenterActionClickListener {
            Toast.makeText(this, "Center Action Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
