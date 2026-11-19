package com.example.pocketlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

//handles user authentication(login and registration)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoggedIn: (String) -> Unit
) {
    //observe the authentication state
    val state by vm.state.collectAsState()

    //Ui state variables
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    //Default to Register so new users create an account first
    var isLoginMode by rememberSaveable { mutableStateOf(false) }

    //UI surface for background and the layout
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // black from your theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //title
            Text(
                if (isLoginMode) "Sign in to PocketLibrary" else "Create your PocketLibrary account",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(20.dp))

            //email input
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (state.error != null) vm.clearError()
                },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            //password input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (state.error != null) vm.clearError()
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            //showing loading indicators
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (isLoginMode) vm.login(email.trim(), password.trim())
                        else vm.register(email.trim(), password.trim())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoginMode) "Login" else "Register")
                }

                Spacer(Modifier.height(8.dp))

                //toggle between login and register modes
                TextButton(onClick = { isLoginMode = !isLoginMode }) {
                    Text(
                        if (isLoginMode) "Don't have an account? Register"
                        else "Already have an account? Sign in"
                    )
                }
            }

            //show errors if theres any
            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            //if login/registration succeeded, notify parent
            state.userId?.let { uid ->
                LaunchedEffect(uid) { onLoggedIn(uid) }
            }
            }
        }
}