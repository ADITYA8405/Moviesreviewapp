package com.example.movieratings.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Section header text — equivalent to Flutter's modified_text(text, size: 26, color: white).
 *
 * Used for "Trending Movies", "Popular TV Shows", "Top Rated Movies" labels.
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
