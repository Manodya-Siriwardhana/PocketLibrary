package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//holds the authentication related UI state
data class AuthUiState(
    val isLoading: Boolean = false,
    val userId: String? = null,
    val error: String? = null
)

//the view model that manages firebase logic
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance() //firebase authentication
    private val _state = MutableStateFlow(AuthUiState()) //internal mutable state
    val state: StateFlow<AuthUiState> = _state //immutable state

    init {

        //if a user is already logged set his or her ID in the UI state
        auth.currentUser?.let { user ->
            _state.value = AuthUiState(userId = user.uid)
        }
    }

    //handling the users login with email and the password
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Email and password required")
            return
        }
        _state.value = _state.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                //updating the state with user iD after successful login
                _state.value = AuthUiState(userId = res.user?.uid)
            }
            .addOnFailureListener { e ->
                //show error message if unsuccessful login
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Login failed")
            }
    }

    //this handles new user registration
    fun register(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _state.value = _state.value.copy(error = "Use a valid email and a password of at least 6 characters")
            return
        }
        _state.value = _state.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                //update state with ID after successful login
                _state.value = AuthUiState(userId = res.user?.uid)
            }
            .addOnFailureListener { e ->
                //showing error message
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Registration failed")
            }
    }

    //logs out the user and resets the state
    fun logout() {
        auth.signOut()
        _state.value = AuthUiState()
    }

    //clearing any errors displayed in the UI
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

//References for authentication
// https://youtu.be/KOnLpNZ4AFc?si=V0AVLl2qFG6R5le2
// https://youtu.be/vI3pK4JvZzY?si=NsWRHrX-evM3Azkr
