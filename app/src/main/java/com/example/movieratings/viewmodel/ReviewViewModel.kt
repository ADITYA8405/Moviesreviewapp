package com.example.movieratings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.model.Review
import com.example.movieratings.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages all UI state for the rating + review section on the Details screen.
 *
 * Responsibilities
 * ─────────────────
 * • Loads the current user's existing review (if any) for the given movie.
 * • Streams all reviews for the movie in real time via [reviews].
 * • Exposes draft rating/text the user is currently editing.
 * • Handles save / delete operations and tracks loading + error state.
 */
class ReviewViewModel : ViewModel() {

    private val repository = ReviewRepository()

    // ── All reviews for the current movie ───────────────────────────────────

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // ── Current user's own review (null = no review yet) ────────────────────

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview.asStateFlow()

    // ── Draft state while the user is composing / editing ───────────────────

    private val _draftRating = MutableStateFlow(0)
    val draftRating: StateFlow<Int> = _draftRating.asStateFlow()

    private val _draftText = MutableStateFlow("")
    val draftText: StateFlow<String> = _draftText.asStateFlow()

    // ── Whether the review form is currently expanded for editing ────────────

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // ── Async operation state ────────────────────────────────────────────────

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Initialisation ───────────────────────────────────────────────────────

    /**
     * Must be called once, passing the TMDb [movieId] and the authenticated
     * [userId] so the ViewModel can scope all Firestore reads/writes correctly.
     */
    fun init(movieId: Int, userId: String, userEmail: String) {
        loadReviews(movieId)
        loadUserReview(movieId, userId, userEmail)
    }

    private fun loadReviews(movieId: Int) {
        viewModelScope.launch {
            try {
                repository.getReviewsFlow(movieId).collect { list ->
                    _reviews.value = list.sortedByDescending { it.updatedAt }
                }
            } catch (e: Exception) {
                // Do not crash the app — just show a non-blocking error hint.
                // getReviewsFlow already emits emptyList() on Firestore errors,
                // so this catch handles unexpected runtime exceptions only.
                _errorMessage.value = "Could not load reviews: ${e.localizedMessage}"
            }
        }
    }

    private fun loadUserReview(movieId: Int, userId: String, userEmail: String) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserReview(movieId, userId)
                _userReview.value = existing
                if (existing != null) {
                    // Pre-fill draft with the saved values so the user can edit them
                    _draftRating.value = existing.rating
                    _draftText.value   = existing.reviewText
                }
            } catch (e: Exception) {
                // Silent failure — user can still write a new review
            }
        }
    }

    // ── Draft field updates ──────────────────────────────────────────────────

    fun setDraftRating(rating: Int) { _draftRating.value = rating }
    fun setDraftText(text: String)  { _draftText.value   = text   }

    fun openEditor() {
        // If there's an existing review, pre-fill from it; otherwise keep defaults
        _userReview.value?.let {
            _draftRating.value = it.rating
            _draftText.value   = it.reviewText
        }
        _isEditing.value = true
    }

    fun cancelEditor() {
        _isEditing.value = false
        _errorMessage.value = null
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveReview(movieId: Int, userId: String, userEmail: String) {
        if (_draftRating.value == 0) {
            _errorMessage.value = "Please select a star rating before saving."
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val review = Review(
                userId     = userId,
                userEmail  = userEmail,
                movieId    = movieId,
                rating     = _draftRating.value,
                reviewText = _draftText.value.trim(),
                createdAt  = _userReview.value?.createdAt ?: System.currentTimeMillis(),
                updatedAt  = System.currentTimeMillis()
            )

            val result = repository.saveReview(review)
            _isSaving.value = false

            if (result.isSuccess) {
                _userReview.value = review
                _isEditing.value  = false
            } else {
                _errorMessage.value =
                    result.exceptionOrNull()?.message ?: "Failed to save review."
            }
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    fun deleteReview(movieId: Int, userId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val result = repository.deleteReview(movieId, userId)
            _isSaving.value = false

            if (result.isSuccess) {
                _userReview.value  = null
                _draftRating.value = 0
                _draftText.value   = ""
                _isEditing.value   = false
            } else {
                _errorMessage.value =
                    result.exceptionOrNull()?.message ?: "Failed to delete review."
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
