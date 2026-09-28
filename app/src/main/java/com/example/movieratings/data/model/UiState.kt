package com.example.movieratings.data.model

/**
 * Represents the three possible states of a UI that loads data from the network.
 *
 * Usage in Composables:
 *   when (state) {
 *       is UiState.Loading -> LoadingView()
 *       is UiState.Success -> ShowData(state.data)
 *       is UiState.Error   -> ErrorView(state.message) { retry() }
 *   }
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
