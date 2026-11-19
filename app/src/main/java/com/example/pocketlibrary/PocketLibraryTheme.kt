package com.example.pocketlibrary

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//dark color scheme for the app
private val DarkColors = darkColorScheme(
    primary = Color(0xFF42A5F5),   //light blue
    onPrimary = Color.Black,
    background = Color.Black,      //full black app background
    onBackground = Color.White,
    surface = Color(0xFF000000),   //dark colors for surfaces/cards
    onSurface = Color.White
)

//default typography
private val AppTypography = androidx.compose.material3.Typography()

//app theme
@Composable
fun PocketLibraryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}




