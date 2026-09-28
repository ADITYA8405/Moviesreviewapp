package com.example.movieratings.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import com.example.movieratings.data.model.MediaItem

/**
 * Lightweight data stored for each saved movie in favourites or watchlist.
 *
 * We store minimal display metadata alongside the TMDb ID so the Library
 * screen can render cards without a network round-trip.
 */
data class SavedMovie(
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val overview: String? = null,
    val voteAverage: Double? = null,
    val releaseDate: String? = null,
    val addedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "movieId"      to movieId,
        "title"        to title,
        "posterPath"   to posterPath,
        "backdropPath" to backdropPath,
        "overview"     to overview,
        "voteAverage"  to voteAverage,
        "releaseDate"  to releaseDate,
        "addedAt"      to addedAt
    )

    fun toMediaItem(): MediaItem = MediaItem(
        id           = movieId,
        title        = title,
        posterPath   = posterPath,
        backdropPath = backdropPath,
        overview     = overview,
        voteAverage  = voteAverage,
        releaseDate  = releaseDate
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): SavedMovie = SavedMovie(
            movieId      = (map["movieId"]      as? Number)?.toInt() ?: 0,
            title        = map["title"]        as? String ?: "",
            posterPath   = map["posterPath"]   as? String,
            backdropPath = map["backdropPath"] as? String,
            overview     = map["overview"]     as? String,
            voteAverage  = (map["voteAverage"] as? Number)?.toDouble(),
            releaseDate  = map["releaseDate"]  as? String,
            addedAt      = (map["addedAt"]     as? Number)?.toLong() ?: 0L
        )
    }
}

/**
 * Firestore repository for user-specific favourites and watchlist.
 *
 * Firestore structure:
 *   users/{userId}/favourites/{movieId}
 *   users/{userId}/watchlist/{movieId}
 *
 * Security rules (to be added):
 *   match /users/{userId}/{document=**} {
 *     allow read, write: if request.auth != null && request.auth.uid == userId;
 *   }
 */
class UserLibraryRepository {

    private val db = FirebaseFirestore.getInstance()
    private val TIMEOUT_MS = 10_000L

    // ── Collection references ────────────────────────────────────────────

    private fun favouritesCollection(userId: String) =
        db.collection("users").document(userId).collection("favourites")

    private fun watchlistCollection(userId: String) =
        db.collection("users").document(userId).collection("watchlist")

    // ── Favourites ───────────────────────────────────────────────────────

    /** Adds a movie to the user's favourites. Uses movieId as doc ID → no duplicates. */
    suspend fun addFavourite(userId: String, movie: SavedMovie): Result<Unit> {
        return try {
            val data = movie.copy(addedAt = System.currentTimeMillis()).toMap()
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    favouritesCollection(userId).document(movie.movieId.toString()).set(data)
                        .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Removes a movie from the user's favourites. */
    suspend fun removeFavourite(userId: String, movieId: Int): Result<Unit> {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    favouritesCollection(userId).document(movieId.toString()).delete()
                        .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Checks whether a movie is in the user's favourites (one-shot). */
    suspend fun isFavourite(userId: String, movieId: Int): Boolean {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    favouritesCollection(userId).document(movieId.toString()).get()
                        .addOnSuccessListener { if (cont.isActive) cont.resume(it.exists()) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(false) }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /** Real-time stream of the user's favourites. */
    fun getFavouritesFlow(userId: String): Flow<List<SavedMovie>> = callbackFlow {
        val registration = favouritesCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { SavedMovie.fromMap(it) }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.addedAt })
            }
        awaitClose { registration.remove() }
    }

    // ── Watchlist ─────────────────────────────────────────────────────────

    /** Adds a movie to the user's watchlist. Uses movieId as doc ID → no duplicates. */
    suspend fun addToWatchlist(userId: String, movie: SavedMovie): Result<Unit> {
        return try {
            val data = movie.copy(addedAt = System.currentTimeMillis()).toMap()
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    watchlistCollection(userId).document(movie.movieId.toString()).set(data)
                        .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Removes a movie from the user's watchlist. */
    suspend fun removeFromWatchlist(userId: String, movieId: Int): Result<Unit> {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    watchlistCollection(userId).document(movieId.toString()).delete()
                        .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Checks whether a movie is in the user's watchlist (one-shot). */
    suspend fun isInWatchlist(userId: String, movieId: Int): Boolean {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    watchlistCollection(userId).document(movieId.toString()).get()
                        .addOnSuccessListener { if (cont.isActive) cont.resume(it.exists()) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(false) }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /** Real-time stream of the user's watchlist. */
    fun getWatchlistFlow(userId: String): Flow<List<SavedMovie>> = callbackFlow {
        val registration = watchlistCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { SavedMovie.fromMap(it) }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.addedAt })
            }
        awaitClose { registration.remove() }
    }
}
