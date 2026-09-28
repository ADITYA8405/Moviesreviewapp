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

    private fun userReviewsCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("reviews")

    // ── Write operations ─────────────────────────────────────────────────────

    /**
     * Saves (or fully replaces) a user's review for [movieId].
     * Because the document ID is the user's UID, a second call simply updates
     * the existing document — no duplicates can form.
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
                            val createdAt = (snap.data?.get("createdAt") as? Number)?.toLong() ?: now
                            if (cont.isActive) cont.resume(createdAt)
                        }
                        task.addOnFailureListener {
                            if (cont.isActive) cont.resume(now)
                        }
                    }
                }
            } catch (_: Exception) {
                now
            }

            val data = review.copy(
                createdAt = existingCreatedAt,
                updatedAt = now
            ).toMap()

            withTimeout(FIRESTORE_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val task1 = reviewsCollection(review.movieId).document(review.userId).set(data)
                    userReviewsCollection(review.userId).document(review.movieId.toString()).set(data)

                    task1.addOnSuccessListener {
                        if (cont.isActive) cont.resume(Unit)
                    }
                    task1.addOnFailureListener { e ->
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
                    userReviewsCollection(userId).document(movieId.toString()).delete()
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
     * Returns a real-time [Flow] of all reviews written by [userId].
     */
    fun getUserReviewsFlow(userId: String): Flow<List<Review>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        listenerRegistration = userReviewsCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Review.fromMap(it) }
                } ?: emptyList()
                trySend(reviews.sortedByDescending { it.updatedAt })
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

    /**
     * Fetches all reviews written by [userId] across all movies.
     */
    suspend fun getAllReviewsByUser(userId: String): List<Review> {
        return try {
            withTimeout(FIRESTORE_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    userReviewsCollection(userId).get()
                        .addOnSuccessListener { snapshot ->
                            val reviews = snapshot.documents.mapNotNull { doc ->
                                doc.data?.let { Review.fromMap(it) }
                            }
                            if (reviews.isNotEmpty()) {
                                if (cont.isActive) cont.resume(reviews)
                            } else {
                                // Fall back to collectionGroup if user collection is empty
                                db.collectionGroup("userReviews")
                                    .whereEqualTo("userId", userId)
                                    .get()
                                    .addOnSuccessListener { groupSnap ->
                                        val groupReviews = groupSnap.documents.mapNotNull { d ->
                                            d.data?.let { Review.fromMap(it) }
                                        }
                                        if (cont.isActive) cont.resume(groupReviews)
                                    }
                                    .addOnFailureListener {
                                        if (cont.isActive) cont.resume(emptyList())
                                    }
                            }
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(emptyList())
                        }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
