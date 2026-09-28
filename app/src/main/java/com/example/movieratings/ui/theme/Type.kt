package com.example.movieratings.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography matching the Flutter app's Google Fonts Roboto usage.
 *
 * Flutter used: GoogleFonts.roboto(color, fontSize)
 * Android's default font IS Roboto, so we use FontFamily.Default.
 */
val MovieTypography = Typography(
    // Section headers — Flutter used size: 26
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        color = White
    ),
    // Movie title on details screen — Flutter used size: 24
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        color = White
    ),
    // Card titles and body text — Flutter used size: 14-16
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = White
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = White
    ),
    // Small labels like release date — Flutter used size: 14
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = LightGray
    )
)
