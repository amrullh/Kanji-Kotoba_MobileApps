package com.amr.kanjikotoba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.amr.kanjikotoba.ui.KanjiKotobaTheme
import com.amr.kanjikotoba.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanjiKotobaTheme {
                MainScreen()
            }
        }
    }
}
