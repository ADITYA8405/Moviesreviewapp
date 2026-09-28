package com.example.movieratings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieratings.data.model.MediaItem

/**
 * A single movie poster card — equivalent to the Flutter Container + DecorationImage
 * inside TrendingMovies and TopRatedMovies widgets.
 *
 * Dimensions: 140dp wide × 200dp tall poster + title below (matching Flutter's 140w × 200h).
 * Uses Coil's AsyncImage instead of Flutter's NetworkImage.
 */
@Composable
fun MovieCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        // Poster image — matches Flutter's Container(height: 200, decoration: BoxDecoration(...))
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Title — matches Flutter's modified_text(size: 14, text: title)
        Text(
            text = item.displayTitle,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
