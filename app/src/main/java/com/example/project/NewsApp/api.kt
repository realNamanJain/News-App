package com.example.project.NewsApp

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("lang") language: String,
        @Query("apikey") apiKey: String
    ): NewsResponse

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("lang") language: String,
        @Query("apikey") apiKey: String
    ): NewsResponse

    @GET("top-headlines")
    suspend fun getTopHeadlinesIndia(
        @Query("lang") language: String,
        @Query("country") country: String,
        @Query("apikey") apiKey: String
    ): NewsResponse

    @GET("top-headlines")
    suspend fun getCategoryHeadlines(
        @Query("category") category: String,
        @Query("lang") language: String,
        @Query("country") country: String,
        @Query("apikey") apiKey: String
    ): NewsResponse
}
