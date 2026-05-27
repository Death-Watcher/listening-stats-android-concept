package com.listeningstats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.listeningstats.app.ui.navigation.AppNavHost
import com.listeningstats.app.ui.theme.ListeningStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListeningStatsTheme {
                AppNavHost()
            }
        }
    }
}
