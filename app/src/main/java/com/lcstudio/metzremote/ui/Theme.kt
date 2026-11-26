package com.lcstudio.metzremote.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Pal {
    val Bg = Color(0xFF0B1226)
    val Bg2 = Color(0xFF16213E)
    val Surface = Color(0xFF1B2540)
    val Accent = Color(0xFF6C8CFF)
    val Text = Color(0xFFEAF0FF)
    val Muted = Color(0xFF8FA0C8)
    val Red = Color(0xFFFF4D5E)
    val Yt = Color(0xFFE62117)
    val Ok = Color(0xFF2FBF86)
}

@Composable
fun LcTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pal.Accent,
            background = Pal.Bg,
            surface = Pal.Surface,
            onSurface = Pal.Text,
            onBackground = Pal.Text,
        ),
        content = content,
    )
}
