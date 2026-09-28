package com.example.movieratings.data.model

/**
 * Represents a comment posted in the public movie discussion.
 *
 * Firestore structure:
 *   discussions/{movieId}/comments/{commentId}
 */
data class DiscussionComment(
    val id: String = "",
    val movieId: Int = 0,
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    /** Display name derived from userName, email or UID. */
    val displayName: String
        get() {
            if (userName.isNotBlank()) return userName
            if (userEmail.isNotBlank()) {
                val prefix = userEmail.substringBefore("@")
                return prefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            return if (userId.length >= 6) "User_${userId.take(6)}" else "User"
        }

    val isEdited: Boolean
        get() = updatedAt > createdAt && createdAt != 0L

    fun toMap(): Map<String, Any> = mapOf(
        "movieId"   to movieId,
        "userId"    to userId,
        "userEmail" to userEmail,
        "userName"  to displayName,
        "text"      to text,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): DiscussionComment = DiscussionComment(
            id        = id,
            movieId   = (map["movieId"]   as? Number)?.toInt() ?: 0,
            userId    = map["userId"]    as? String ?: "",
            userEmail = map["userEmail"] as? String ?: "",
            userName  = (map["userName"]  as? String) ?: (map["displayName"] as? String) ?: "",
            text      = map["text"]      as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
