package com.example.project.NewsApp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // Correct placement of verticalArrangement
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Spacing between fields
            isError = email.isEmpty() && !isLoading // Error indication if email is empty
        )

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = password.isEmpty() && !isLoading // Error indication if password is empty
        )

        // Confirm Password TextField
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            isError = confirmPassword.isEmpty() && !isLoading // Error indication if confirm password is empty
        )

        // Sign Up Button
        Button(
            onClick = {
                if (password == confirmPassword) {
                    isLoading = true
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        onSuccess = { isSuccess ->
                            isLoading = false
                            if (isSuccess) {
                                Toast.makeText(context, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            } else {
                                Toast.makeText(context, "Sign-Up Failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFailure = { errorMessage ->
                            isLoading = false
                            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Sign Up")
            }
        }

        // Error messages for fields
        if (email.isEmpty()) {
            Text(text = "Please enter your email", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        } else if (password.isEmpty()) {
            Text(text = "Please enter a password", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        } else if (confirmPassword.isEmpty()) {
            Text(text = "Please confirm your password", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
