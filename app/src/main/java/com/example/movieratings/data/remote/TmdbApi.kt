package com.example.movieratings.data.remote

import com.example.movieratings.data.model.TmdbResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the TMDb API.
 *
 * Each function maps to one of the three endpoints the Flutter app used:
 *   - tmdb.v3.trending.getTrending()  →  GET /trending/all/day
 *   - tmdb.v3.movies.getTopRated()    →  GET /movie/top_rated
 *   - tmdb.v3.tv.getPopular()         →  GET /tv/popular
 *
 * All functions are suspend (coroutine-based), which replaces Flutter's async/await.
 */
interface TmdbApi {

    @GET("trending/all/day")
    suspend fun getTrending(
        @Query("api_key") apiKey: String
    ): TmdbResponse

    @GET("movie/top_rated")
    suspend fun getTopRated(
        @Query("api_key") apiKey: String
    ): TmdbResponse

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("api_key") apiKey: String
    ): TmdbResponse

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TmdbResponse
}
