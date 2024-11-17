package com.example.project.NewsApp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ForgotPasswordScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = email.isEmpty() && !isLoading // Show error if email is empty
        )

        // Forgot Password Button
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    isLoading = true
                    authViewModel.resetPassword(
                        email = email,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // Navigate back to previous screen
                        },
                        onFailure = { errorMessage ->
                            isLoading = false
                            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Send Reset Email")
            }
        }

        // If no email is entered, show error message
        if (email.isEmpty()) {
            Text(
                text = "Please enter your email",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
