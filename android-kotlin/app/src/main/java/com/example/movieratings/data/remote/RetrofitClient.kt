package com.example.movieratings.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Singleton that provides the configured Retrofit instance.
 *
 * This is the Kotlin equivalent of the Flutter app's TMDB client setup:
 *   TMDB(ApiKeys(apiKey, readAccessToken), logConfig: ...)
 *
 * Key differences:
 *   - Uses Retrofit instead of the tmdb_api Dart package
 *   - Uses Kotlinx Serialization instead of Dart's built-in JSON handling
 *   - ignoreUnknownKeys = true so we don't crash on extra API fields
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    // Configure the JSON parser to skip fields we don't model
    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: TmdbApi = retrofit.create(TmdbApi::class.java)
}
