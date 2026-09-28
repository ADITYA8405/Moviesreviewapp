package com.example.movieratings.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieratings.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Operation state for login/signup submit actions.
 */
sealed interface AuthOperationState {
    data object Idle : AuthOperationState
    data object Loading : AuthOperationState
    data object Success : AuthOperationState
    data class Error(val message: String) : AuthOperationState
}

/**
 * ViewModel managing authentication state and actions.
 */
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    /** Flow emitting the current Firebase user. */
    val currentUser: StateFlow<FirebaseUser?> = repository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getCurrentUser()
        )

    private val _operationState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val operationState: StateFlow<AuthOperationState> = _operationState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty()) {
            _operationState.value = AuthOperationState.Error("Please enter your email address.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _operationState.value = AuthOperationState.Error("Please enter a valid email address.")
            return
        }
        if (trimmedPassword.isEmpty()) {
            _operationState.value = AuthOperationState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            _operationState.value = AuthOperationState.Loading
            val result = repository.login(trimmedEmail, trimmedPassword)
            result.fold(
                onSuccess = {
                    _operationState.value = AuthOperationState.Success
                },
                onFailure = { error ->
                    _operationState.value = AuthOperationState.Error(
                        error.message ?: "Login failed. Please check your credentials."
                    )
                }
            )
        }
    }

    fun signup(email: String, password: String, confirmPassword: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirm = confirmPassword.trim()

        if (trimmedEmail.isEmpty()) {
            _operationState.value = AuthOperationState.Error("Please enter your email address.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _operationState.value = AuthOperationState.Error("Please enter a valid email address.")
            return
        }
        if (trimmedPassword.isEmpty()) {
            _operationState.value = AuthOperationState.Error("Please enter a password.")
            return
        }
        if (trimmedPassword.length < 6) {
            _operationState.value = AuthOperationState.Error("Password must be at least 6 characters.")
            return
        }
        if (trimmedPassword != trimmedConfirm) {
            _operationState.value = AuthOperationState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _operationState.value = AuthOperationState.Loading
            val result = repository.signup(trimmedEmail, trimmedPassword)
            result.fold(
                onSuccess = {
                    _operationState.value = AuthOperationState.Success
                },
                onFailure = { error ->
                    _operationState.value = AuthOperationState.Error(
                        error.message ?: "Account creation failed."
                    )
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _operationState.value = AuthOperationState.Idle
    }

    fun clearError() {
        if (_operationState.value is AuthOperationState.Error) {
            _operationState.value = AuthOperationState.Idle
        }
    }
}
