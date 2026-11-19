package com.example.pocketlibrary

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String = "title,author_name,first_publish_year,cover_i,first_sentence,ratings_average,ratings_count,edition_count,key",
        @Query("limit") limit: Int = 20
    ): SearchResponse
}