package com.example.pocketlibrary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel,
    libraryVm: LibraryViewModel,
    userId: String,
    onBookClick: (Doc) -> Unit
) {
    //collect current state from ViewModels
    val state by vm.state.collectAsStateWithLifecycle()
    val libraryBooks by libraryVm.books.collectAsState()

    //determine number of grib columns based on orientation
    val columns = when (LocalConfiguration.current.orientation) {
        android.content.res.Configuration.ORIENTATION_LANDSCAPE -> 3
        else -> 2
    }

    val gridState = rememberLazyGridState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        //search Bar
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search books or authors") },
            placeholder = { Text("Enter title, author...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.search() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        //display number of results if available
        AnimatedVisibility(
            visible = state.results.isNotEmpty() && !state.loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "${state.results.size} results found",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(Modifier.fillMaxSize()) {
            when {
                state.loading -> {
                    //show loading indicator
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Searching...", color = Color.Gray)
                    }
                }

                state.error != null -> {
                    //show error message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    }
                }

                state.query.isNotBlank() && state.results.isEmpty() -> {
                    //no results found
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text("No results found", color = Color.Gray)
                    }
                }

                state.query.isBlank() -> {
                    //prompt user to start searching
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text("Start Searching...", color = Color.Gray, fontSize = 20.sp)
                    }
                }

                else -> {
                    //display search results
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        state = gridState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.results) { doc ->
                            //check if book is already in library
                            val isSaved = libraryBooks.any {
                                it.title.equals(doc.title, ignoreCase = true) &&
                                        it.author.equals(doc.authorLine, ignoreCase = true)
                            }

                            val savedBook = libraryBooks.find {
                                it.title.equals(doc.title, ignoreCase = true) &&
                                        it.author.equals(doc.authorLine, ignoreCase = true)
                            }

                            //display enhanced book card
                            EnhancedBookCard(
                                doc = doc,
                                isSaved = isSaved,
                                onClick = { onBookClick(doc) },
                                onSave = {
                                    val entity = BookEntity(
                                        title = doc.title ?: "Untitled",
                                        author = doc.authorLine,
                                        year = doc.first_publish_year,
                                        coverUrl = doc.coverUrlMedium ?: doc.coverUrlSmall,
                                        userId = userId
                                    )
                                    libraryVm.saveBook(entity)
                                },
                                onRemove = {
                                    savedBook?.let { libraryVm.deleteBook(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedBookCard(
    doc: Doc,
    isSaved: Boolean,
    onClick: () -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            //book cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                AsyncImage(
                    model = doc.coverUrlMedium ?: doc.coverUrlSmall,
                    contentDescription = doc.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                //favorite button
                IconButton(
                    onClick = {
                        if (isSaved) onRemove() else onSave()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from Library" else "Add to Library",
                        tint = if (isSaved) Color(0xFFE91E63) else Color.White
                    )
                }

                //rating badge
                doc.ratings_average?.let { rating ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        color = Color(0xFF000000).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                DecimalFormat("#.#").format(rating),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            //book info kike title, author and year
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = doc.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = doc.author_name?.firstOrNull() ?: "Unknown Author",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                doc.first_publish_year?.let { year ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Published $year",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}