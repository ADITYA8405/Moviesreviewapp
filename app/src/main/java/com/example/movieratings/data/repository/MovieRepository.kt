package com.example.movieratings.data.repository

import com.example.movieratings.BuildConfig
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.remote.RetrofitClient

/**
 * Single source of truth for movie/TV data.
 *
 * The ViewModel calls this repository, which in turn calls the Retrofit API.
 * This keeps networking logic out of the UI layer.
 *
 * In the Flutter app, API calls were made directly inside the StatefulWidget's
 * loadmovies() method. This separation is a key architectural improvement.
 */
class MovieRepository {

    private val api = RetrofitClient.api
    private val apiKey = BuildConfig.TMDB_API_KEY

    /** Fetches trending movies/shows for the day. */
    suspend fun getTrending(): List<MediaItem> {
        return api.getTrending(apiKey).results
    }

    /** Fetches top-rated movies of all time. */
    suspend fun getTopRated(): List<MediaItem> {
        return api.getTopRated(apiKey).results
    }

    /** Fetches currently popular TV shows. */
    suspend fun getPopularTv(): List<MediaItem> {
        return api.getPopularTv(apiKey).results
    }

    /** Searches movies and TV shows by title/name query. */
    suspend fun searchMulti(query: String): List<MediaItem> {
        return api.searchMulti(apiKey, query).results
    }
}
