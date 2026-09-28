package com.example.movieratings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
 * A single TV show card — uses the backdrop image instead of the poster.
 *
 * Equivalent to Flutter's TV widget's ListView.builder items.
 * Dimensions: 250dp wide × 140dp tall (matching Flutter's 250w × 140h).
 * Uses backdrop_path instead of poster_path for a wider landscape look.
 */
@Composable
fun TvCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(250.dp)
            .padding(end = 5.dp)
            .clickable(onClick = onClick)
    ) {
        // Backdrop image — matches Flutter's Container(height: 140, decoration: BoxDecoration(...))
        AsyncImage(
            model = item.backdropUrl,
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(140.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Show name — matches Flutter's modified_text(size: 15, text: original_name)
        Text(
            text = item.displayTitle,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
