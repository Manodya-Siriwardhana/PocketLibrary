package com.example.pocketlibrary

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseService {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private fun userBooks(uid: String) =
        firestore.collection("users").document(uid).collection("favourites")

    suspend fun upsertBook(book: BookEntity): String {
        val uid = auth.currentUser?.uid ?: return book.cloudId ?: ""
        val col = userBooks(uid)

        val stableId = book.cloudId ?: "${book.title}_${book.author}_${book.userId}".hashCode().toString()
        val docRef = col.document(stableId)

        val payload = hashMapOf(
            "title" to book.title,
            "author" to book.author,
            "year" to book.year,
            "coverUrl" to book.coverUrl,
            "personalPhotoUri" to book.personalPhotoUri,
            "userId" to book.userId,
            "cloudId" to stableId
        )

        docRef.set(payload).await()
        return stableId
    }


     //Delete a book from Firestore

    suspend fun deleteBook(book: BookEntity) {
        val uid = auth.currentUser?.uid ?: return
        val cloudId = book.cloudId ?: return
        userBooks(uid).document(cloudId).delete().await()
    }


      //Fetch all books from Firestore

    suspend fun fetchBooks(uid: String): List<BookEntity> {
        val snapshot = userBooks(uid).get().await()
        val list = mutableListOf<BookEntity>()

        for (doc in snapshot.documents) {
            val data = doc.data ?: continue
            list.add(
                BookEntity(
                    id = 0,
                    title = data["title"] as? String ?: "Untitled",
                    author = data["author"] as? String ?: "Unknown",
                    year = (data["year"] as? Long)?.toInt(),
                    coverUrl = data["coverUrl"] as? String,
                    personalPhotoUri = data["personalPhotoUri"] as? String,
                    userId = data["userId"] as? String ?: uid,
                    cloudId = doc.id
                )
            )
        }
        return list
    }


     //Upload book photo

    suspend fun uploadBookPhoto(uid: String, bookId: String, file: File): String? {
        return try {
            val storageRef = storage.reference.child("book_photos/$uid/${bookId}.jpg")
            val uri = Uri.fromFile(file)

            // Upload JPEG to Firebase Storage
            storageRef.putFile(uri).await()

            // Retrieve HTTPS URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            //  Link the download URL back to Firestore
            firestore.collection("users")
                .document(uid)
                .collection("favourites")
                .document(bookId)
                .update("personalPhotoUri", downloadUrl)
                .await()

            downloadUrl
        } catch (e: Exception) {
            null
        }
    }



    fun listenToBooks(uid: String, onChange: suspend (List<BookEntity>) -> Unit) {
        userBooks(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Firestore listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val books = try {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    BookEntity(
                        id = 0,
                        title = data["title"] as? String ?: "Untitled",
                        author = data["author"] as? String ?: "Unknown",
                        year = (data["year"] as? Long)?.toInt(),
                        coverUrl = data["coverUrl"] as? String,
                        personalPhotoUri = data["personalPhotoUri"] as? String,
                        userId = data["userId"] as? String ?: uid,
                        cloudId = doc.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            CoroutineScope(Dispatchers.IO).launch {
                onChange(books)
            }
        }
    }
}
