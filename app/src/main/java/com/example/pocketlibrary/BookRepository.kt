package com.example.pocketlibrary

import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val dao: BookDao,
    val firebase: FirebaseService
) {

    fun getAll(uid: String): Flow<List<BookEntity>> = dao.getAllBooks(uid)

    fun search(uid: String, query: String): Flow<List<BookEntity>> = dao.searchBooks(uid, query)

    suspend fun insert(book: BookEntity) {
        val rowId = dao.insertBook(book)
        var local = book.copy(id = rowId.toInt())

        try {
            val cloudId = firebase.upsertBook(local)
            if (local.cloudId != cloudId) {
                local = local.copy(cloudId = cloudId)
                dao.updateBook(local)
            }
        } catch (_: Exception) {
        }
    }

    suspend fun update(book: BookEntity) {
        try {
            val cloudId = firebase.upsertBook(book)
            val updated = if (book.cloudId != cloudId) book.copy(cloudId = cloudId) else book
            dao.updateBook(updated)
        } catch (_: Exception) {
            dao.updateBook(book)
        }
    }

    suspend fun delete(book: BookEntity) {
        dao.deleteBook(book)
        try { firebase.deleteBook(book) } catch (_: Exception) {}
    }


    suspend fun syncFromCloud(uid: String) {
        try {
            val remote = firebase.fetchBooks(uid)
            val localBooks = dao.getAllBooksOnce(uid)

            for (rb in remote) {
                val match = localBooks.find {
                    it.cloudId == rb.cloudId ||
                            (it.title == rb.title && it.author == rb.author)
                }

                if (match == null) {
                    //Insert only if it doesn’t exist locally
                    dao.insertBook(rb.copy(id = 0))
                } else {
                    //Update existing record (merge data)
                    dao.updateBook(
                        match.copy(
                            title = rb.title,
                            author = rb.author,
                            year = rb.year,
                            coverUrl = rb.coverUrl,
                            personalPhotoUri = rb.personalPhotoUri,
                            cloudId = rb.cloudId ?: match.cloudId
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun pushLocalToCloud(uid: String) {
        try {
            val locals = dao.getAllBooksOnce(uid)
            for (b in locals) {
                if (b.cloudId.isNullOrBlank()) {
                    val id = firebase.upsertBook(b)
                    dao.updateBook(b.copy(cloudId = id))
                }
            }
        } catch (_: Exception) { }
    }


    fun startRealtimeSync(uid: String) {
        firebase.listenToBooks(uid) { remoteBooks ->
            try {
                //Get a snapshot of current local books for the user
                val locals = dao.getAllBooksOnce(uid)

                for (rb in remoteBooks) {
                    //Check for existing match by cloudId
                    val match = locals.find {
                        it.cloudId == rb.cloudId ||
                                (it.title.equals(rb.title, ignoreCase = true)
                                        && it.author.equals(rb.author, ignoreCase = true))
                    }

                    if (match == null) {
                        //Insert only if no matching local record exists
                        dao.insertBook(rb.copy(id = 0))
                    } else {
                        //Update the existing record instead of duplicating
                        dao.updateBook(
                            match.copy(
                                title = rb.title,
                                author = rb.author,
                                year = rb.year,
                                coverUrl = rb.coverUrl,
                                personalPhotoUri = rb.personalPhotoUri,
                                cloudId = rb.cloudId ?: match.cloudId
                            )
                        )
                    }
                }

                //remove local books that no longer exist in Firestore
                val remoteIds = remoteBooks.mapNotNull { it.cloudId }
                for (local in locals) {
                    if (local.cloudId != null && local.cloudId !in remoteIds) {
                        dao.deleteBook(local)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
