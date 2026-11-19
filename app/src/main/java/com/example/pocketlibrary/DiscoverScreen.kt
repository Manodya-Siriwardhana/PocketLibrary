package com.example.pocketlibrary


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

//displays categorized book sections
@Composable
fun DiscoverScreen(
    vm: DiscoverViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBookClick: (Doc) -> Unit
) {
    val state by vm.state.collectAsState()

    //Main scrollable layout with spacing
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        //header title
        item {
            Text("Discover", style = MaterialTheme.typography.titleLarge)
        }

        //for loading error or displaying content
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.error != null -> item { Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error) }
            else -> {
                //discover Picks section
                item {
                    SectionRow(
                        title = "Discover Picks",
                        books = state.discoverBooks.take(10),
                        onBookClick = onBookClick,
                        cardType = CardType.Small
                    )
                }

                //popular now sectionm
                item {
                    SectionRow(
                        title = "Popular Now",
                        books = state.popularBooks.take(10),
                        onBookClick = onBookClick,
                        cardType = CardType.Large
                    )
                }

                //comedy section
                item {
                    SectionRow(
                        title = "Comedy",
                        books = state.comedyBooks.take(10),
                        onBookClick = onBookClick,
                        cardType = CardType.Small
                    )
                }

                //drama section
                item {
                    SectionRow(
                        title = "Drama",
                        books = state.dramaBooks.take(10),
                        onBookClick = onBookClick,
                        cardType = CardType.Small
                    )
                }
            }
        }
    }
}

//defines card size types used in different sections
enum class CardType { Small, Large }

//displays a section header
@Composable
fun SectionRow(
    title: String,
    books: List<Doc>,
    onBookClick: (Doc) -> Unit,
    cardType: CardType
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(books) { doc ->
                when (cardType) {
                    CardType.Small -> SmallBookCard(doc, onBookClick)
                    CardType.Large -> LargeBookCard(doc, onBookClick)
                }
            }
        }
    }
}

//small version of book card
@Composable
fun SmallBookCard(doc: Doc, onClick: (Doc) -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick(doc) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .padding(6.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //book cover image
            AsyncImage(
                model = doc.coverUrlMedium,
                contentDescription = doc.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(175.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(6.dp))
            //book title
            Text(doc.title ?: "Untitled", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            //authors name
            Text(
                doc.author_name?.firstOrNull() ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

//large book cards(popular now)
@Composable
fun LargeBookCard(doc: Doc, onClick: (Doc) -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick(doc) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                //image
                model = doc.coverUrlLarge,
                contentDescription = doc.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(Modifier.height(12.dp))

            //book title and author
            Text(
                doc.title ?: "Untitled",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                doc.author_name?.firstOrNull() ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}