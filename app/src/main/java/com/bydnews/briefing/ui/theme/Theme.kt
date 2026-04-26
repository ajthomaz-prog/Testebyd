package com.bydnews.briefing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val CarColors = darkColorScheme(
    primary = Color(0xFF2962FF),
    onPrimary = Color.White,
    background = Color(0xFF0B0F14),
    surface = Color(0xFF141A22),
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LargeTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp),
    headlineLarge = TextStyle(fontSize = 32.sp),
    headlineMedium = TextStyle(fontSize = 26.sp),
    titleLarge = TextStyle(fontSize = 22.sp),
    bodyLarge = TextStyle(fontSize = 18.sp),
    bodyMedium = TextStyle(fontSize = 16.sp),
    labelLarge = TextStyle(fontSize = 18.sp),
)

@Composable
fun BydBriefingTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = CarColors,
        typography = LargeTypography,
        content = content,
    )
}
