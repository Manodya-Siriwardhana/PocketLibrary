package com.example.pocketlibrary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//represents the response returned by the open library API
@JsonClass(generateAdapter = true)
data class SearchResponse(
    val numFound: Int? = null,
    val docs: List<Doc> = emptyList()
)

//represent a single book from the open library
//each Doc instance includes infromation about a books title, authors etc
@JsonClass(generateAdapter = true)
data class Doc(
    val title: String? = null,
    val author_name: List<String>? = null,
    val first_publish_year: Int? = null,
    val cover_i: Long? = null,
    val first_sentence: List<String>? = null,
    val ratings_average: Double? = null,
    val ratings_count: Long? = null,
    val edition_count: Int? = null,
    @Json(name = "key")
    val workKey: String? = null
) {

    //Helper properties
    val authorLine: String get() = author_name?.joinToString() ?: "Unknown"
    val coverUrlSmall: String? get() = cover_i?.let { "https://covers.openlibrary.org/b/id/${it}-S.jpg" }
    val coverUrlMedium: String? get() = cover_i?.let { "https://covers.openlibrary.org/b/id/${it}-M.jpg" }
    val coverUrlLarge: String? get() = cover_i?.let { "https://covers.openlibrary.org/b/id/${it}-L.jpg"}
}