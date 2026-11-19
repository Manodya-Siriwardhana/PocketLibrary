package com.example.pocketlibrary


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


//data class for discover screen
data class DiscoverUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val discoverBooks: List<Doc> = emptyList(),
    val popularBooks: List<Doc> = emptyList(),
    val comedyBooks: List<Doc> = emptyList(),
    val dramaBooks: List<Doc> = emptyList()
)

//view model for managing the discover sreen state
class DiscoverViewModel : ViewModel() {
    //backing field for state
    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state

    init {
        //fetching all the book categories
        fetchBooks()
    }

    //fetch different categories of books from the API
    fun fetchBooks() {
        viewModelScope.launch {
            //starting the loading process
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                //perform API calls
                val discoverResp = NetworkModule.api.search("classics")
                val popularResp = NetworkModule.api.search("bestseller")
                val comedyResp = NetworkModule.api.search("comedy")
                val dramaResp = NetworkModule.api.search("drama")

                //update UI state
                _state.value = _state.value.copy(
                    loading = false,
                    discoverBooks = discoverResp.docs,
                    popularBooks = popularResp.docs,
                    comedyBooks = comedyResp.docs,
                    dramaBooks = dramaResp.docs
                )
            } catch (t: Throwable) {
                //handle errors
                _state.value = _state.value.copy(loading = false, error = t.message)
            }
        }
    }
}


