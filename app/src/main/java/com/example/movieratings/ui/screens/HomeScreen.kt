package com.example.movieratings.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.ui.components.MovieCard
import com.example.movieratings.ui.components.MovieCarousel
import com.example.movieratings.ui.components.TvCard
import com.example.movieratings.viewmodel.HomeViewModel

/**
 * Home screen — the Kotlin equivalent of Flutter's Home StatefulWidget in main.dart.
 *
 * Flutter structure:
 *   Scaffold(
 *     appBar: AppBar(title: "Flutter Movie App ❤️"),
 *     body: ListView(children: [TrendingMovies, TV, TopRatedMovies])
 *   )
 *
 * Compose structure:
 *   Scaffold(
 *     topBar: TopAppBar(title = "Movie Ratings App ❤️"),
 *     content: LazyColumn { Trending, Popular TV, Top Rated }
 *   )
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onItemClick: (MediaItem) -> Unit
) {
    // Collect the three StateFlows as Compose State
    val trendingState by viewModel.trendingState.collectAsState()
    val topRatedState by viewModel.topRatedState.collectAsState()
    val tvState by viewModel.tvState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movie Ratings App ❤\uFE0F",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        // LazyColumn = Flutter's ListView (vertical)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Section 1: Trending Movies (same order as Flutter's ListView children)
            item {
                MovieCarousel(
                    title = "Trending Movies",
                    state = trendingState,
                    onItemClick = onItemClick,
                    onRetry = { viewModel.loadTrending() }
                ) { item ->
                    MovieCard(item = item, onClick = { onItemClick(item) })
                }
            }

            // Section 2: Popular TV Shows
            item {
                MovieCarousel(
                    title = "Popular TV Shows",
                    state = tvState,
                    carouselHeight = 200.dp,
                    onItemClick = onItemClick,
                    onRetry = { viewModel.loadPopularTv() }
                ) { item ->
                    TvCard(item = item, onClick = { onItemClick(item) })
                }
            }

            // Section 3: Top Rated Movies
            item {
                MovieCarousel(
                    title = "Top Rated Movies",
                    state = topRatedState,
                    onItemClick = onItemClick,
                    onRetry = { viewModel.loadTopRated() }
                ) { item ->
                    MovieCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}
