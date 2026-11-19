package com.example.pocketlibrary

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [Index(value = ["userId", "cloudId"], unique = true)]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val year: Int?,
    val coverUrl: String?,
    val personalPhotoUri: String? = null,
    val userId: String,
    val cloudId: String? = null // variable to hold the Firestore doc id
)
