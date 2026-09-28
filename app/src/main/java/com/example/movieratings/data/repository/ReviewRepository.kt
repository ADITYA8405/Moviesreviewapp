package com.example.movieratings.data.repository

import com.example.movieratings.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firestore repository for user reviews.
 *
 * Firestore structure:
 *   reviews (collection)
 *     └── {movieId} (document)
 *           └── userReviews (sub-collection)
 *                 └── {userId} (document)  ← one per user per movie
 *
 * Using {userId} as the document ID guarantees at most one review per user per
 * movie without any additional query-side deduplication.
 *
 * NOTE: Make sure Cloud Firestore is enabled in your Firebase project console
 * and that security rules allow authenticated users to read/write their own docs.
 * Recommended rules:
 *   match /reviews/{movieId}/userReviews/{userId} {
 *     allow read: if true;
 *     allow write: if request.auth != null && request.auth.uid == userId;
 *   }
 */
class ReviewRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Timeout applied to every individual Firestore read/write (10 seconds)
    private val FIRESTORE_TIMEOUT_MS = 10_000L

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun reviewsCollection(movieId: Int) =
        db.collection("reviews")
            .document(movieId.toString())
            .collection("userReviews")

    // ── Write operations ─────────────────────────────────────────────────────

    /**
     * Saves (or fully replaces) a user's review for [movieId].
     * Because the document ID is the user's UID, a second call simply updates
     * the existing document — no duplicates can form.
     *
     * Uses [suspendCancellableCoroutine] so the operation can be cancelled when
     * the ViewModel scope is cleared, and [withTimeout] to prevent infinite hangs
     * when Firestore is offline.
     */
    suspend fun saveReview(review: Review): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // Fetch existing createdAt (fallback to now on any failure)
            val existingCreatedAt: Long = try {
                withTimeout(FIRESTORE_TIMEOUT_MS) {
                    suspendCancellableCoroutine { cont ->
                        val task = reviewsCollection(review.movieId)
                            .document(review.userId)
                            .get()
                        task.addOnSuccessListener { snap ->
                            if (cont.isActive) cont.resume(snap.getLong("createdAt") ?: now)
                        }
                        task.addOnFailureListener {
                            // Non-fatal — treat as first-time save
                            if (cont.isActive) cont.resume(now)
                        }
                    }
                }
            } catch (_: Exception) {
                now // timeout or cancellation — safe fallback
            }

            val data = review.copy(
                createdAt = existingCreatedAt,
                updatedAt = now
            ).toMap()

            withTimeout(FIRESTORE_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val task = reviewsCollection(review.movieId)
                        .document(review.userId)
                        .set(data)
                    task.addOnSuccessListener {
                        if (cont.isActive) cont.resume(Unit)
                    }
                    task.addOnFailureListener { e ->
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permanently deletes the current user's review for [movieId].
     */
    suspend fun deleteReview(movieId: Int, userId: String): Result<Unit> {
        return try {
            withTimeout(FIRESTORE_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val task = reviewsCollection(movieId).document(userId).delete()
                    task.addOnSuccessListener {
                        if (cont.isActive) cont.resume(Unit)
                    }
                    task.addOnFailureListener { e ->
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Read operations ──────────────────────────────────────────────────────

    /**
     * Returns a real-time [Flow] of all reviews for the given [movieId].
     * Emits an empty list on Firestore errors so the caller never crashes.
     */
    fun getReviewsFlow(movieId: Int): Flow<List<Review>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        listenerRegistration = reviewsCollection(movieId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Emit an empty list instead of closing the flow with an exception.
                    // Closing with an exception propagates through collect() and crashes
                    // the ViewModel's coroutine unless the caller has an explicit try-catch.
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Review.fromMap(it) }
                } ?: emptyList()
                trySend(reviews)
            }

        awaitClose { listenerRegistration?.remove() }
    }

    /**
     * Fetches the current user's existing review for [movieId] once (non-realtime).
     * Returns null if no review exists yet or on any error.
     */
    suspend fun getUserReview(movieId: Int, userId: String): Review? {
        return try {
            withTimeout(FIRESTORE_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val task = reviewsCollection(movieId).document(userId).get()
                    task.addOnSuccessListener { snap ->
                        if (cont.isActive) cont.resume(snap.data?.let { Review.fromMap(it) })
                    }
                    task.addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
