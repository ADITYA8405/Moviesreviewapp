package com.example.movieratings.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.ui.theme.White

/**
 * Movie/TV details screen — the Kotlin equivalent of Flutter's Description StatelessWidget.
 *
 * Flutter structure (from description.dart):
 *   Scaffold → ListView → [
 *     Banner image (250h) with rating overlay,
 *     Movie title (24sp),
 *     Release date (14sp),
 *     Row: poster (120w×200h) + description text
 *   ]
 *
 * Improvement over Flutter: Added a back button (Flutter version had no explicit back navigation).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    item: MediaItem?,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (item == null) {
            // Safety fallback — should not happen in normal flow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Movie not found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        // Scrollable content — matches Flutter's ListView
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner image with rating overlay
            // Matches Flutter's Container(height: 250, child: Stack([Image, Positioned(rating)]))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                AsyncImage(
                    model = item.backdropUrl,
                    contentDescription = "${item.displayTitle} backdrop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating overlay — matches Flutter's Positioned(bottom: 10, left: 10, ...)
                Text(
                    text = "⭐ Average Rating - ${item.voteAverage ?: "N/A"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Movie title — matches Flutter's Padding(all: 10, modified_text(size: 24))
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            // Release date — matches Flutter's Padding(left: 10, "Releasing On - ...")
            Text(
                text = "Releasing On - ${item.displayDate}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 10.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Poster + description row
            // Matches Flutter's Row([Container(120w×200h image), Flexible(description)])
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Poster thumbnail
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = "${item.displayTitle} poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(120.dp)
                        .height(200.dp)
                )

                // Description text — matches Flutter's Flexible(Padding(all: 10, modified_text(size: 16)))
                Text(
                    text = item.overview ?: "No description available for this movie.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
