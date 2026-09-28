package com.example.movieratings.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * App-wide dark theme matching the Flutter app's appearance.
 *
 * Flutter used: ThemeData(brightness: Brightness.dark)
 * Kotlin equivalent: Material 3 darkColorScheme with customized colors.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Red,
    background = Black,
    surface = DarkSurface,
    onBackground = White,
    onSurface = White
)

@Composable
fun MovieRatingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MovieTypography,
        content = content
    )
}
