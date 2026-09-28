package com.example.movieratings.data.repository

import com.example.movieratings.data.model.DiscussionComment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firestore repository for public movie discussion comments.
 *
 * Firestore structure:
 *   discussions/{movieId}/comments/{commentId}
 */
class DiscussionRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TIMEOUT_MS = 10_000L

    private fun commentsCollection(movieId: Int) =
        db.collection("discussions")
            .document(movieId.toString())
            .collection("comments")

    /**
     * Real-time stream of all discussion comments for [movieId],
     * ordered by creation timestamp ascending (oldest first, like a chat feed).
     */
    fun getCommentsFlow(movieId: Int): Flow<List<DiscussionComment>> = callbackFlow {
        var registration: ListenerRegistration? = null

        registration = commentsCollection(movieId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { DiscussionComment.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(comments)
            }

        awaitClose { registration?.remove() }
    }

    /**
     * Adds a new comment to the discussion for [movieId].
     */
    suspend fun addComment(
        movieId: Int,
        userId: String,
        userEmail: String,
        text: String
    ): Result<Unit> {
        return try {
            val comment = DiscussionComment(
                movieId   = movieId,
                userId    = userId,
                userEmail = userEmail,
                text      = text.trim(),
                createdAt = System.currentTimeMillis()
            )
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    commentsCollection(movieId)
                        .add(comment.toMap())
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(Unit)
                        }
                        .addOnFailureListener { e ->
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
     * Updates/edits the text of an existing comment in the discussion for [movieId].
     */
    suspend fun updateComment(
        movieId: Int,
        commentId: String,
        newText: String
    ): Result<Unit> {
        return try {
            val updates = mapOf<String, Any>(
                "text"      to newText.trim(),
                "updatedAt" to System.currentTimeMillis()
            )
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    commentsCollection(movieId)
                        .document(commentId)
                        .update(updates)
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(Unit)
                        }
                        .addOnFailureListener { e ->
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
     * Deletes a specific comment from the movie's discussion.
     */
    suspend fun deleteComment(movieId: Int, commentId: String): Result<Unit> {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    commentsCollection(movieId)
                        .document(commentId)
                        .delete()
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(Unit)
                        }
                        .addOnFailureListener { e ->
                            if (cont.isActive) cont.resumeWithException(e)
                        }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
