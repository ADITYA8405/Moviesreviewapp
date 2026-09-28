package com.example.movieratings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.model.Review
import com.example.movieratings.data.repository.ReviewRepository
import com.example.movieratings.data.repository.SavedMovie
import com.example.movieratings.data.repository.UserLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Library/Profile screen.
 *
 * Exposes three independent lists:
 *   • favourites  (Firestore — user-scoped)
 *   • watchlist   (Firestore — user-scoped)
 *   • myReviews   (Firestore — queried from the existing review collections)
 *
 * Also used by DetailsScreen to check/toggle favourite + watchlist status
 * for the currently displayed movie.
 */
class UserLibraryViewModel : ViewModel() {

    private val libraryRepo = UserLibraryRepository()
    private val reviewRepo  = ReviewRepository()

    // ── Library lists (for LibraryScreen & DetailsScreen) ────────────────

    private val _favourites = MutableStateFlow<List<SavedMovie>>(emptyList())
    val favourites: StateFlow<List<SavedMovie>> = _favourites.asStateFlow()

    private val _watchlist = MutableStateFlow<List<SavedMovie>>(emptyList())
    val watchlist: StateFlow<List<SavedMovie>> = _watchlist.asStateFlow()

    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toggleError = MutableStateFlow<String?>(null)
    val toggleError: StateFlow<String?> = _toggleError.asStateFlow()

    private var activeUserId: String? = null

    // ── Initialisation ───────────────────────────────────────────────────

    /**
     * Initializes real-time listeners for the authenticated user's library.
     */
    fun initLibrary(userId: String) {
        if (activeUserId == userId) return
        activeUserId = userId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                libraryRepo.getFavouritesFlow(userId).collect { list ->
                    _favourites.value = list
                    _isLoading.value = false
                }
            } catch (_: Exception) {
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            try {
                libraryRepo.getWatchlistFlow(userId).collect { list ->
                    _watchlist.value = list
                }
            } catch (_: Exception) { }
        }

        viewModelScope.launch {
            try {
                reviewRepo.getUserReviewsFlow(userId).collect { list ->
                    if (list.isNotEmpty()) {
                        _myReviews.value = list
                    } else {
                        // Fallback to one-time query
                        val allReviews = reviewRepo.getAllReviewsByUser(userId)
                        _myReviews.value = allReviews.sortedByDescending { it.updatedAt }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    /** Refresh reviews when returning from editing */
    fun refreshReviews(userId: String) {
        viewModelScope.launch {
            try {
                val reviews = reviewRepo.getAllReviewsByUser(userId)
                if (reviews.isNotEmpty()) {
                    _myReviews.value = reviews.sortedByDescending { it.updatedAt }
                }
            } catch (_: Exception) { }
        }
    }

    // ── Toggle operations ────────────────────────────────────────────────

    fun toggleFavourite(userId: String, movie: SavedMovie) {
        initLibrary(userId)
        viewModelScope.launch {
            _toggleError.value = null
            val currentlyFav = _favourites.value.any { it.movieId == movie.movieId }

            // Optimistic update
            if (currentlyFav) {
                _favourites.value = _favourites.value.filter { it.movieId != movie.movieId }
            } else {
                _favourites.value = listOf(movie.copy(addedAt = System.currentTimeMillis())) + _favourites.value
            }

            val result = if (currentlyFav) {
                libraryRepo.removeFavourite(userId, movie.movieId)
            } else {
                libraryRepo.addFavourite(userId, movie)
            }

            if (result.isFailure) {
                // Revert on failure
                if (currentlyFav) {
                    _favourites.value = listOf(movie) + _favourites.value
                } else {
                    _favourites.value = _favourites.value.filter { it.movieId != movie.movieId }
                }
                _toggleError.value = result.exceptionOrNull()?.message ?: "Failed to update favourites."
            }
        }
    }

    fun toggleWatchlist(userId: String, movie: SavedMovie) {
        initLibrary(userId)
        viewModelScope.launch {
            _toggleError.value = null
            val currentlyIn = _watchlist.value.any { it.movieId == movie.movieId }

            // Optimistic update
            if (currentlyIn) {
                _watchlist.value = _watchlist.value.filter { it.movieId != movie.movieId }
            } else {
                _watchlist.value = listOf(movie.copy(addedAt = System.currentTimeMillis())) + _watchlist.value
            }

            val result = if (currentlyIn) {
                libraryRepo.removeFromWatchlist(userId, movie.movieId)
            } else {
                libraryRepo.addToWatchlist(userId, movie)
            }

            if (result.isFailure) {
                // Revert on failure
                if (currentlyIn) {
                    _watchlist.value = listOf(movie) + _watchlist.value
                } else {
                    _watchlist.value = _watchlist.value.filter { it.movieId != movie.movieId }
                }
                _toggleError.value = result.exceptionOrNull()?.message ?: "Failed to update watchlist."
            }
        }
    }

    fun clearToggleError() { _toggleError.value = null }
}
