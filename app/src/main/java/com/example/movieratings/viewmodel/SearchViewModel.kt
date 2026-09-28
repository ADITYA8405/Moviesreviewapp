package com.example.movieratings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed UI state for SearchScreen representing empty query, loading, success, no results, and error.
 */
sealed interface SearchUiState {
    data object EmptyQuery : SearchUiState
    data object Loading : SearchUiState
    data class Success(val items: List<MediaItem>) : SearchUiState
    data object NoResults : SearchUiState
    data class Error(val message: String) : SearchUiState
}

/**
 * ViewModel responsible for search state and calling TMDb search API via MovieRepository.
 */
class SearchViewModel : ViewModel() {

    private val repository = MovieRepository()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.EmptyQuery)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.EmptyQuery
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce typing
            performSearch(newQuery)
        }
    }

    fun performSearch(queryToSearch: String = _query.value) {
        if (queryToSearch.isBlank()) {
            _uiState.value = SearchUiState.EmptyQuery
            return
        }
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val results = repository.searchMulti(queryToSearch)
                if (results.isEmpty()) {
                    _uiState.value = SearchUiState.NoResults
                } else {
                    _uiState.value = SearchUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(
                    e.message ?: "Failed to perform search. Check network connection."
                )
            }
        }
    }
}
