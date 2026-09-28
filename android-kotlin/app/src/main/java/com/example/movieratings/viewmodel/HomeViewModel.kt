package com.example.movieratings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.model.UiState
import com.example.movieratings.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen — the Kotlin replacement for Flutter's StatefulWidget + setState().
 *
 * In the Flutter app, _MyWidgetState held three Lists (trendingmovies, topratedmovies, tvresult)
 * and called setState() to update them. Here, we use StateFlow which automatically notifies
 * the Compose UI when data changes — no manual "setState" needed.
 *
 * Architecture: HomeScreen (UI) → HomeViewModel → MovieRepository → TmdbApi (Retrofit)
 */
class HomeViewModel : ViewModel() {

    private val repository = MovieRepository()

    // Each section has its own state so they can load/fail independently
    private val _trendingState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Loading)
    val trendingState: StateFlow<UiState<List<MediaItem>>> = _trendingState.asStateFlow()

    private val _topRatedState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Loading)
    val topRatedState: StateFlow<UiState<List<MediaItem>>> = _topRatedState.asStateFlow()

    private val _tvState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Loading)
    val tvState: StateFlow<UiState<List<MediaItem>>> = _tvState.asStateFlow()

    // In-memory cache of all loaded items, keyed by ID.
    // The Details screen looks up a movie here instead of re-fetching from the API.
    private val allItems = mutableMapOf<Int, MediaItem>()

    init {
        // Equivalent of Flutter's initState() → loadmovies()
        loadAll()
    }

    /** Loads all three sections. Called on init and when the user taps "Retry". */
    fun loadAll() {
        loadTrending()
        loadTopRated()
        loadPopularTv()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _trendingState.value = UiState.Loading
            try {
                val items = repository.getTrending()
                items.forEach { allItems[it.id] = it }
                _trendingState.value = UiState.Success(items)
            } catch (e: Exception) {
                _trendingState.value = UiState.Error(
                    e.message ?: "Failed to load trending movies"
                )
            }
        }
    }

    fun loadTopRated() {
        viewModelScope.launch {
            _topRatedState.value = UiState.Loading
            try {
                val items = repository.getTopRated()
                items.forEach { allItems[it.id] = it }
                _topRatedState.value = UiState.Success(items)
            } catch (e: Exception) {
                _topRatedState.value = UiState.Error(
                    e.message ?: "Failed to load top rated movies"
                )
            }
        }
    }

    fun loadPopularTv() {
        viewModelScope.launch {
            _tvState.value = UiState.Loading
            try {
                val items = repository.getPopularTv()
                items.forEach { allItems[it.id] = it }
                _tvState.value = UiState.Success(items)
            } catch (e: Exception) {
                _tvState.value = UiState.Error(
                    e.message ?: "Failed to load popular TV shows"
                )
            }
        }
    }

    /** Looks up a previously loaded item by its TMDb ID. Used by the Details screen. */
    fun getItemById(id: Int): MediaItem? = allItems[id]
}
