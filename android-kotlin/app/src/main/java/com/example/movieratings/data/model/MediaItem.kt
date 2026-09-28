package com.example.movieratings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the top-level response from any TMDb list endpoint.
 * The "results" array contains the actual movie/TV items.
 */
@Serializable
data class TmdbResponse(
    val page: Int,
    val results: List<MediaItem>
)

/**
 * A single movie or TV show from TMDb.
 *
 * Movies use [title] and [releaseDate].
 * TV shows use [originalName] and [firstAirDate].
 * Both share poster, backdrop, overview, and rating fields.
 */
@Serializable
data class MediaItem(
    val id: Int,

    // Movie-specific fields
    val title: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,

    // TV-specific fields
    @SerialName("original_name")
    val originalName: String? = null,
    @SerialName("first_air_date")
    val firstAirDate: String? = null,

    // Shared fields
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    val overview: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null
) {
    /** Returns whichever title is available — movie title or TV show name. */
    val displayTitle: String
        get() = title ?: originalName ?: "No Title"

    /** Returns whichever date is available — movie release or TV first air. */
    val displayDate: String
        get() = releaseDate ?: firstAirDate ?: "Unknown"

    /** Full URL for the poster image, with a placeholder fallback. */
    val posterUrl: String
        get() = posterPath?.let { "$IMAGE_BASE_URL$it" }
            ?: PLACEHOLDER_POSTER

    /** Full URL for the backdrop/banner image, with a placeholder fallback. */
    val backdropUrl: String
        get() = backdropPath?.let { "$IMAGE_BASE_URL$it" }
            ?: PLACEHOLDER_BACKDROP

    companion object {
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
        private const val PLACEHOLDER_POSTER = "https://via.placeholder.com/300x450?text=No+Poster"
        private const val PLACEHOLDER_BACKDROP = "https://via.placeholder.com/500x300?text=No+Image"
    }
}
