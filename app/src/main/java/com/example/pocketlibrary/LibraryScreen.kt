package com.example.pocketlibrary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File

//checking for camera permission and returining boolean indicating if permission granted
@Composable
fun rememberCameraPermissionState(): Boolean {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    //permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    //automaticcaly request permission when composable loads
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return hasPermission
}

//main screen showing users personal library of books
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel, userId: String) {
    val books by vm.books.collectAsState()
    val query by vm.query.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    //show add book dialog
    if (showDialog) {
        AddBookDialog(
            userId = userId,
            onDismiss = { showDialog = false },
            onSave = {
                vm.saveBook(it)
                showDialog = false
                Toast.makeText(context, "Book added: ${it.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF42A5F5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book", tint = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            //screen title
            Text(
                "My Library",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            //search bar for filtering books
            OutlinedTextField(
                value = query,
                onValueChange = vm::searchBooks,
                label = { Text("Search books") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            //all books in users library
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(books) { book ->
                    BookCard(
                        book = book,
                        onDelete = {
                            vm.deleteBook(book)
                            Toast.makeText(
                                context,
                                "Deleted: ${book.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onShare = { shareBook(context, book) },
                        onPhotoTaken = { bitmap ->
                            vm.attachPhoto(context, book, bitmap)
                            Toast.makeText(
                                context,
                                "Updated photo for: ${book.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

//individual book card with image, details and action buttons
@Composable
fun BookCard(
    book: BookEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onPhotoTaken: (Bitmap?) -> Unit
) {
    val context = LocalContext.current
    val hasCameraPermission = rememberCameraPermissionState()

    //camera for taking pics
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        onPhotoTaken(bitmap)
    }

    //choose the image source
    val photoModel: Any? = when {
        book.personalPhotoUri.isNullOrBlank() -> book.coverUrl
        book.personalPhotoUri!!.startsWith("http") -> book.personalPhotoUri
        else -> File(book.personalPhotoUri!!)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            //book image and details
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photoModel,
                    contentDescription = "Cover of ${book.title}",
                    modifier = Modifier
                        .size(90.dp, 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        book.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            //action buttons like camera share and delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookActionIcon(Icons.Default.CameraAlt, "Take Photo", Color(0xFF42A5F5)) {
                    if (hasCameraPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
                    }
                }

                Spacer(Modifier.width(20.dp))
                BookActionIcon(Icons.Default.Share, "Share", Color.Gray, onClick = onShare)
                Spacer(Modifier.width(20.dp))
                BookActionIcon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = Color(0xFFE91E63),
                    onClick = onDelete
                )
            }
        }
    }
}

//small reusable icon button
@Composable
fun BookActionIcon(
    icon: ImageVector,
    contentDesc: String,
    tint: Color = Color.Gray,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDesc,
        tint = tint,
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick)
    )
}

//shares book details with an external app
fun shareBook(context: Context, book: BookEntity) {
    // Check if book has a cover URL
    val hasApiData = !book.coverUrl.isNullOrBlank()

    val shareText = buildString {
        append("Title: ${book.title}\n")
        append("Author: ${book.author}\n")
        if (book.year != null) {
            append("Published: ${book.year}\n")
        }

        // Only include link for books from the API
        if (hasApiData) {
            val searchQuery = "${book.title} ${book.author}".replace(" ", "+")
            val openLibraryUrl = "https://openlibrary.org/search?q=$searchQuery"
            append("\nFind it here: $openLibraryUrl")
        }
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Book Recommendation: ${book.title}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
}

//dialog for adding a new book entry
@Composable
fun AddBookDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSave: (BookEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") }
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { newValue ->
                        // Only allow digits
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            year = newValue
                        }
                    },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        BookEntity(
                            title = title,
                            author = author,
                            year = year.toIntOrNull(),
                            coverUrl = null,
                            userId = userId
                        )
                    )
                },
                enabled = title.isNotBlank() && author.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}