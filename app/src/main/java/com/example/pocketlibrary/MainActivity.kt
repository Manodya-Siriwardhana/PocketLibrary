package com.example.pocketlibrary

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

//represent different screens withiin the pocket library
sealed class Screen {
    object Discover : Screen()
    object Search : Screen()
    object Library : Screen()
    data class Detail(val doc: Doc) : Screen()
}

//main entry point for the library and it handles app initialization and splash display also authentication
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PocketLibraryApp
        val repo = app.repository

        setContent {
            PocketLibraryTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    //display splash
                    SplashScreen { showSplash = false }
                } else {
                    val authVm: AuthViewModel = viewModel()
                    val authState by authVm.state.collectAsState()

                    //show login screen if not logged
                    if (authState.userId == null) {
                        LoginScreen(vm = authVm, onLoggedIn = { })
                    } else {
                        //or else show main app content
                        LibraryAppContent(
                            repo = repo,
                            userId = authState.userId!!,
                            onExit = { finish() }
                        )
                    }
                }
            }
        }
    }
}

//display bottom navigation and handles navigation stack
@Composable
fun LibraryAppContent(repo: BookRepository, userId: String, onExit: () -> Unit) {
    //simple navigation stack for back navifgation
    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Discover)) }
    val currentScreen = screenStack.last()

    val searchVm: SearchViewModel = viewModel()
    val libraryVm = remember { LibraryViewModel(repo, userId) }
    val authVm: AuthViewModel = viewModel()

    //handle the physical back button behavior
    BackHandler(enabled = true) {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            //exit app when on the last screen
            onExit()
        }
    }

    //automatically sync Firestore  Room when user logs in
    LaunchedEffect(userId) {
        libraryVm.syncBooks()
    }

    Scaffold(
        bottomBar = {
            //hiding thr navigation bar in book detail screen
            if (currentScreen !is Screen.Detail) {
                NavigationBar(containerColor = Color(0xFF000000)) {
                    //discover tab
                    NavigationBarItem(
                        selected = currentScreen is Screen.Discover,
                        onClick = {
                            if (currentScreen !is Screen.Discover) {
                                screenStack = screenStack + Screen.Discover
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Discover") },
                        label = { Text("Discover") },
                        colors = navColors()
                    )
                    //search tab
                    NavigationBarItem(
                        selected = currentScreen is Screen.Search,
                        onClick = {
                            if (currentScreen !is Screen.Search) {
                                screenStack = screenStack + Screen.Search
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        colors = navColors()
                    )
                    //library tab
                    NavigationBarItem(
                        selected = currentScreen is Screen.Library,
                        onClick = {
                            if (currentScreen !is Screen.Library) {
                                screenStack = screenStack + Screen.Library
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "My Library") },
                        label = { Text("My Library") },
                        colors = navColors()
                    )
                    //logout button
                    NavigationBarItem(
                        selected = false,
                        onClick = { authVm.logout() },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Logout") },
                        label = { Text("Logout") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Red,
                            selectedTextColor = Color.Red,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        //main content area
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                //discover
                Screen.Discover -> DiscoverScreen(
                    onBookClick = { doc -> screenStack = screenStack + Screen.Detail(doc) }
                )

                //search
                Screen.Search -> SearchScreen(
                    vm = searchVm,
                    libraryVm = libraryVm,
                    userId = userId,
                    onBookClick = { doc -> screenStack = screenStack + Screen.Detail(doc) }
                )

                //library
                Screen.Library -> {
                    LaunchedEffect(Unit) { libraryVm.syncBooks() }
                    LibraryScreen(libraryVm, userId)
                }

                //book details
                is Screen.Detail -> BookDetailScreen(
                    doc = screen.doc,
                    isSaved = false,
                    onSaveToggle = {
                        libraryVm.saveBook(
                            BookEntity(
                                title = screen.doc.title ?: "Untitled",
                                author = screen.doc.authorLine,
                                year = screen.doc.first_publish_year,
                                coverUrl = screen.doc.coverUrlMedium,
                                userId = userId
                            )
                        )
                    },
                    onBack = {
                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                    },
                    searchVm = searchVm,
                    libraryVm = libraryVm,
                    onBookClick = { newDoc ->
                        screenStack = screenStack + Screen.Detail(newDoc)
                    }
                )
            }
        }
    }
}

//defining the color scheme for bottom bars
@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color(0xFF42A5F5),
    selectedTextColor = Color(0xFF42A5F5),
    unselectedIconColor = Color.Gray,
    unselectedTextColor = Color.Gray,
    indicatorColor = Color.Transparent
)

//custom calss that initialize the room database
class PocketLibraryApp : Application() {
    lateinit var repository: BookRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(this)
        repository = BookRepository(db.bookDao(), FirebaseService())
    }
}