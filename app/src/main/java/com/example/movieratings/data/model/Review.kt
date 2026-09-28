package com.example.movieratings.data.model

/**
 * Represents a user review stored in Firestore.
 *
 * Firestore path: reviews/{movieId}/userReviews/{userId}
 *
 * One review per user per movie is enforced by using the Firebase UID as the
 * document ID, so writing a second review simply overwrites the first.
 */
data class Review(
    /** Firebase UID of the reviewer. */
    val userId: String = "",

    /** Display name or email shown in the review list. */
    val userEmail: String = "",

    /** TMDb movie/TV item ID. */
    val movieId: Int = 0,

    /** User rating from 1 to 5 (inclusive). */
    val rating: Int = 0,

    /** Optional free-text review body. */
    val reviewText: String = "",

    /** Epoch millis when the review was first created. */
    val createdAt: Long = 0L,

    /** Epoch millis when the review was last updated. */
    val updatedAt: Long = 0L
) {
    /**
     * Converts this Review to a plain Map for Firestore set/update operations.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "userId"     to userId,
        "userEmail"  to userEmail,
        "movieId"    to movieId,
        "rating"     to rating,
        "reviewText" to reviewText,
        "createdAt"  to createdAt,
        "updatedAt"  to updatedAt
    )

    companion object {
        /** Deserialises a Firestore document snapshot into a [Review]. */
        fun fromMap(map: Map<String, Any?>): Review = Review(
            userId     = map["userId"]     as? String ?: "",
            userEmail  = map["userEmail"]  as? String ?: "",
            movieId    = (map["movieId"]   as? Number)?.toInt() ?: 0,
            rating     = (map["rating"]    as? Number)?.toInt() ?: 0,
            reviewText = map["reviewText"] as? String ?: "",
            createdAt  = (map["createdAt"]  as? Number)?.toLong() ?: 0L,
            updatedAt  = (map["updatedAt"]  as? Number)?.toLong() ?: 0L
        )
    }
}
