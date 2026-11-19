package com.example.pocketlibrary

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.DecimalFormat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    doc: Doc,  //book details
    isSaved: Boolean, //checks if the book is saved in the library
    onSaveToggle: () -> Unit, //callback to toggle the save state
    onBack: () -> Unit,  //callback for back navigation
    searchVm: SearchViewModel = viewModel(),
    onBookClick: (Doc) -> Unit = {},  //when a similar book is clicked
    libraryVm: LibraryViewModel
) {
    val scrollState = rememberScrollState()
    val similarBooks by searchVm.similarBooks.collectAsState()

    //fetch similar books by author when screen opens
    LaunchedEffect(doc.author_name) {
        searchVm.fetchSimilarBooks(doc.author_name?.firstOrNull())
    }

    //checks the library and check if the current book is already in it
    val libraryBooks by libraryVm.books.collectAsState()
    val isBookInLibrary = libraryBooks.any {
        it.title.equals(doc.title, ignoreCase = true) && it.author.equals(doc.authorLine, ignoreCase = true)
    }

    // Get context for Toast
    val context = LocalContext.current //for toast messages

    Scaffold(
        topBar = {
            //top app bar with the back button
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // cover section of the bbook
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = doc.coverUrlLarge,
                    contentDescription = doc.title,
                    modifier = Modifier
                        .height(300.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            //book details section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //authors name
                Text(
                    text = doc.author_name?.firstOrNull() ?: "Unknown Author",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(6.dp))

                //book titlee
                Text(
                    text = doc.title ?: "Untitled",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(20.dp))

                //ratings and reviews section
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //book ratings
                    val ratingValue = doc.ratings_average?.let { DecimalFormat("#.#").format(it) } ?: "N/A"
                    RoundedStatBox(
                        icon = Icons.Default.Star,
                        value = ratingValue,
                        label = "Rating",
                        iconTint = Color(0xFFFFD700) // Gold
                    )

                    //number of reviews
                    val reviewCountValue = doc.ratings_count?.let { formatNumber(it) } ?: "N/A"
                    RoundedStatBox(
                        icon = Icons.Default.RateReview,
                        value = reviewCountValue,
                        label = "Reviews",
                        iconTint = Color(0xFF42A5F5) // Light Blue
                    )

                    //edition count
                    val editionsValue = doc.edition_count?.let { formatNumber(it.toLong()) } ?: "N/A"
                    RoundedStatBox(
                        icon = Icons.Default.LibraryBooks,
                        value = editionsValue,
                        label = "Editions",
                        iconTint = Color(0xFF66BB6A) // Green
                    )
                }

                Spacer(Modifier.height(20.dp))

                //save button
                Button(
                    onClick = {
                        if (!isBookInLibrary) {
                            onSaveToggle()
                            Toast.makeText(context, "${doc.title} added to your library!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "This book is already in your library!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                ) {
                    Text(
                        if (isSaved) "Remove from Library" else "Want to Read",
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(20.dp))

                //Description
                Text(
                    text = doc.first_sentence?.firstOrNull() ?: "No description available.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(32.dp))

                //similar books section
                if (similarBooks.isNotEmpty()) {
                    Text(
                        "Similar Books",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(similarBooks) { book ->
                            SmallBookCard(doc = book, onClick = onBookClick)
                        }
                    }
                }
            }
        }
    }
}

//helper function to  format numbers
fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> "${DecimalFormat("#.#").format(number / 1_000_000.0)}M"
        number >= 1_000 -> "${DecimalFormat("#.#").format(number / 1_000.0)}K"
        else -> number.toString()
    }
}

//reusable UI component for displaying rounded stat box
@Composable
fun RoundedStatBox(icon: ImageVector, value: String, label: String, iconTint: Color) {
    Box(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.69f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}