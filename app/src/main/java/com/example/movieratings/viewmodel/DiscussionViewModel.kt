package com.example.movieratings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.model.DiscussionComment
import com.example.movieratings.data.repository.DiscussionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing public movie discussions and comment streams.
 */
class DiscussionViewModel : ViewModel() {

    private val repository = DiscussionRepository()

    private val _comments = MutableStateFlow<List<DiscussionComment>>(emptyList())
    val comments: StateFlow<List<DiscussionComment>> = _comments.asStateFlow()

    private val _draftComment = MutableStateFlow("")
    val draftComment: StateFlow<String> = _draftComment.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    // ── Edit comment state ───────────────────────────────────────────────────

    private val _editingCommentId = MutableStateFlow<String?>(null)
    val editingCommentId: StateFlow<String?> = _editingCommentId.asStateFlow()

    private val _editingText = MutableStateFlow("")
    val editingText: StateFlow<String> = _editingText.asStateFlow()

    private val _isSavingEdit = MutableStateFlow(false)
    val isSavingEdit: StateFlow<Boolean> = _isSavingEdit.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentMovieId: Int? = null
    private var observeJob: Job? = null

    /**
     * Initializes real-time comment observer for [movieId].
     */
    fun init(movieId: Int) {
        if (currentMovieId == movieId) return
        currentMovieId = movieId
        _comments.value = emptyList()
        _draftComment.value = ""
        _editingCommentId.value = null
        _editingText.value = ""
        _errorMessage.value = null

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            try {
                repository.getCommentsFlow(movieId).collect { list ->
                    _comments.value = list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load comments: ${e.localizedMessage}"
            }
        }
    }

    fun setDraftComment(text: String) {
        _draftComment.value = text
    }

    fun postComment(movieId: Int, userId: String, userEmail: String) {
        val text = _draftComment.value.trim()
        if (text.isEmpty()) {
            _errorMessage.value = "Comment cannot be empty."
            return
        }

        viewModelScope.launch {
            _isPosting.value = true
            _errorMessage.value = null

            val result = repository.addComment(movieId, userId, userEmail, text)
            _isPosting.value = false

            if (result.isSuccess) {
                _draftComment.value = ""
            } else {
                _errorMessage.value =
                    result.exceptionOrNull()?.message ?: "Failed to post comment. Please check your connection."
            }
        }
    }

    fun startEditing(comment: DiscussionComment) {
        _editingCommentId.value = comment.id
        _editingText.value = comment.text
        _errorMessage.value = null
    }

    fun setEditingText(text: String) {
        _editingText.value = text
    }

    fun cancelEditing() {
        _editingCommentId.value = null
        _editingText.value = ""
        _errorMessage.value = null
    }

    fun saveEditedComment(movieId: Int) {
        val commentId = _editingCommentId.value ?: return
        val newText = _editingText.value.trim()
        if (newText.isEmpty()) {
            _errorMessage.value = "Comment cannot be empty."
            return
        }

        viewModelScope.launch {
            _isSavingEdit.value = true
            _errorMessage.value = null

            val result = repository.updateComment(movieId, commentId, newText)
            _isSavingEdit.value = false

            if (result.isSuccess) {
                _editingCommentId.value = null
                _editingText.value = ""
            } else {
                _errorMessage.value =
                    result.exceptionOrNull()?.message ?: "Failed to update comment."
            }
        }
    }

    fun deleteComment(movieId: Int, commentId: String) {
        viewModelScope.launch {
            val result = repository.deleteComment(movieId, commentId)
            if (result.isFailure) {
                _errorMessage.value =
                    result.exceptionOrNull()?.message ?: "Failed to delete comment."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
