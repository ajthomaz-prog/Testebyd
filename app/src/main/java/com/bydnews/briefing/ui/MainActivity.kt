package com.bydnews.briefing.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bydnews.briefing.ui.nav.AppNav
import com.bydnews.briefing.ui.theme.BydBriefingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialBriefingId = intent.getStringExtra("briefingId")
        setContent {
            BydBriefingTheme {
                AppNav(initialBriefingId = initialBriefingId)
            }
        }
    }
}
