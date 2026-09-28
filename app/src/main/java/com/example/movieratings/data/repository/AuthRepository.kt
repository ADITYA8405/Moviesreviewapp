package com.example.movieratings.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Repository for Firebase Authentication.
 *
 * Provides coroutine-based login, signup, logout, and a Flow of the current auth state.
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Flow emitting the current FirebaseUser when authentication state changes. */
    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Returns current user synchronously. */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /** Logs in an existing user with email and password. */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = suspendCoroutine<FirebaseUser> { continuation ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.user?.let {
                                continuation.resume(it)
                            } ?: continuation.resumeWithException(
                                Exception("Authentication succeeded but user was null.")
                            )
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Login failed.")
                            )
                        }
                    }
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    /** Creates a new user account with email and password. */
    suspend fun signup(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = suspendCoroutine<FirebaseUser> { continuation ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.user?.let {
                                continuation.resume(it)
                            } ?: continuation.resumeWithException(
                                Exception("Account creation succeeded but user was null.")
                            )
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Signup failed.")
                            )
                        }
                    }
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    /** Signs out the current user. */
    fun logout() {
        auth.signOut()
    }

    /** Maps Firebase authentication exceptions to clean, user-friendly messages. */
    private fun mapAuthException(e: Exception): String {
        val msg = e.message ?: ""
        if (msg.contains("API key not valid", ignoreCase = true) || msg.contains("API key", ignoreCase = true)) {
            return "Firebase configuration required: Download google-services.json from your Firebase Console and place it into app/google-services.json."
        }
        return when (e) {
            is FirebaseAuthInvalidUserException ->
                "No account found with this email address."
            is FirebaseAuthInvalidCredentialsException ->
                "Incorrect email or password."
            is FirebaseAuthWeakPasswordException ->
                "Password is too weak. Please use at least 6 characters."
            is FirebaseAuthUserCollisionException ->
                "An account already exists with this email address."
            is FirebaseNetworkException ->
                "Network error. Please check your internet connection."
            else ->
                e.localizedMessage ?: "Authentication failed. Please check your details and try again."
        }
    }
}
