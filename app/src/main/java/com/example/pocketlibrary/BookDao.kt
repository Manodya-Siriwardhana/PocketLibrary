package com.example.pocketlibrary

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("SELECT * FROM books WHERE userId = :uid ORDER BY title ASC")
    fun getAllBooks(uid: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE userId = :uid AND (title LIKE :query OR author LIKE :query) ORDER BY title ASC")
    fun searchBooks(uid: String, query: String): Flow<List<BookEntity>>

    // helpers to sync data
    @Query("SELECT * FROM books WHERE userId = :uid")
    suspend fun getAllBooksOnce(uid: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE userId = :uid AND cloudId = :cloudId LIMIT 1")
    suspend fun findByCloudId(uid: String, cloudId: String): BookEntity?
}
