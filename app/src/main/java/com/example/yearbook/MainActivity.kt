package com.example.yearbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.yearbook.ui.theme.YearBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YearBookTheme(darkTheme = false, dynamicColor = false) {
                YearBookDiaryApp()
            }
        }
    }
}