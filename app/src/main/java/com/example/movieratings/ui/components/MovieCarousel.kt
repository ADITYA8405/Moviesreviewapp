package com.example.movieratings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.model.UiState

/**
 * A complete section: header + horizontal carousel of items.
 *
 * Equivalent to the Flutter Column(children: [modified_text(...), SizedBox, Container(ListView.builder)])
 * pattern used in TrendingMovies, TopRatedMovies, and TV widgets.
 *
 * LazyRow is the Compose equivalent of Flutter's ListView.builder(scrollDirection: Axis.horizontal).
 */
@Composable
fun MovieCarousel(
    title: String,
    state: UiState<List<MediaItem>>,
    carouselHeight: Dp = 270.dp,
    onItemClick: (MediaItem) -> Unit,
    onRetry: () -> Unit,
    itemContent: @Composable (MediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        SectionHeader(title = title)

        when (state) {
            is UiState.Loading -> {
                LoadingView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(carouselHeight)
                )
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(carouselHeight)
                )
            }
            is UiState.Success -> {
                LazyRow(
                    modifier = Modifier.height(carouselHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 10.dp)
                ) {
                    items(
                        items = state.data,
                        key = { it.id }
                    ) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}
