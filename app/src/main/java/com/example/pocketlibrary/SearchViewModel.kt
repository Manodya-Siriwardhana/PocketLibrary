package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//holds the current state of the search UI
data class SearchUiState(
    val query: String = "", //current search input
    val loading: Boolean = false, //loading indicator
    val error: String? = null,   //error message if search fails
    val results: List<Doc> = emptyList()
)

//viewmodel reponsible for handling search operations
class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    private var searchJob: Job? = null

    //updates the search query
    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q, error = null)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce user input
            search()
        }
    }

    //performs a search based on the current query
    fun search() {
        val q = _state.value.query.trim()
        if (q.isEmpty()) {
            //clear results if query is empty
            _state.value = _state.value.copy(results = emptyList(), loading = false, error = null)
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = NetworkModule.api.search(q)
                _state.value = _state.value.copy(loading = false, results = resp.docs)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(loading = false, error = t.message ?: "Network error")
            }
        }
    }

    //similar books feature
    private val _similarBooks = MutableStateFlow<List<Doc>>(emptyList())
    val similarBooks: StateFlow<List<Doc>> = _similarBooks

    //fetches up to 10 books by the same author
    fun fetchSimilarBooks(author: String?) {
        if (author.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val resp = NetworkModule.api.search(author)
                _similarBooks.value = resp.docs.take(10) //limit to 10 similar books
            } catch (_: Exception) {
                _similarBooks.value = emptyList() //reset on error
            }
        }
    }

}
