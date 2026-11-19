package com.example.pocketlibrary

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LibraryViewModel(
    private val repo: BookRepository,
    private val userId: String
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val books: StateFlow<List<BookEntity>> =
        _query.flatMapLatest { q ->
            if (q.isBlank()) repo.getAll(userId)
            else repo.search(userId, "%$q%")
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repo.startRealtimeSync(userId)
            repo.syncFromCloud(userId)
            repo.pushLocalToCloud(userId)
        }
    }

    fun searchBooks(q: String) {
        _query.value = q
    }

    fun saveBook(book: BookEntity) {
        viewModelScope.launch {
            try {
                repo.insert(book.copy(userId = userId))
                repo.pushLocalToCloud(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    fun deleteBook(book: BookEntity) {
        viewModelScope.launch { repo.delete(book) }
    }

    fun syncBooks() {
        viewModelScope.launch {
            repo.syncFromCloud(userId)
            repo.pushLocalToCloud(userId)
        }
    }


    fun attachPhoto(context: Context, book: BookEntity, bitmap: Bitmap?) {
        if (bitmap == null) return
        viewModelScope.launch {
            try {
                //save locally for offline preview
                val file = File(context.filesDir, "book_${book.id}.jpg")
                file.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                //update local entity for immediate display
                var updated = book.copy(personalPhotoUri = file.absolutePath)
                repo.update(updated)

                //ensure Firestore document exists
                val cloudId = repo.firebase.upsertBook(updated)
                updated = updated.copy(cloudId = cloudId)

                //upload actual JPEG to Firebase Storage
                val downloadUrl = repo.firebase.uploadBookPhoto(userId, cloudId, file)

                if (downloadUrl != null) {
                    val finalBook = updated.copy(personalPhotoUri = downloadUrl)
                    repo.update(finalBook)
                    repo.firebase.upsertBook(finalBook)

                } else {
                    println("Upload failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
