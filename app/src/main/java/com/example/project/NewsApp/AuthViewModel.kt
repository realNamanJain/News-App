package com.example.project.NewsApp
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    // Function to handle user sign-up
    fun signUp(
        email: String,
        password: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AuthViewModel", "Sign-up process started for email: $email")
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d(
                    "AuthViewModel",
                    "User created: ${result.user?.email}, UID: ${result.user?.uid}"
                )

                // Switch to Main thread for UI callback
                withContext(Dispatchers.Main) {
                    onSuccess(true)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign Up failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    // Function to handle user sign-in
    fun signIn(
        email: String,
        password: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User UID is null")

                // Initialize Firestore for the signed-in user
                initializeUserFirestore(uid)

                withContext(Dispatchers.Main) {
                    onSuccess(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }


    // Function to get the current user UID
    fun getCurrentUser(): String? {
        val user = auth.currentUser
        return user?.uid.also {
            if (it != null) {
                Log.d("AuthViewModel", "Current user UID: $it")
            } else {
                Log.d("AuthViewModel", "No user is currently signed in.")
            }
        }
    }

    // Function to handle forgot password functionality
    fun resetPassword(
        email: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AuthViewModel", "Sending password reset email to: $email")
                auth.sendPasswordResetEmail(email).await()
                Log.d("AuthViewModel", "Password reset email sent successfully to: $email")

                // Switch to Main thread for UI callback
                withContext(Dispatchers.Main) {
                    onSuccess(true)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to send password reset email: ${e.message}")
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    // Function to handle user sign-out
    fun signOut(onSuccess: (Boolean) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.signOut() // Sign out the user
                Log.d("AuthViewModel", "User signed out successfully")

                // Switch to Main thread for UI callback
                withContext(Dispatchers.Main) {
                    onSuccess(true) // Notify success
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign out failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Unknown error") // Notify failure
                }
            }
        }
    }
}

private fun initializeUserFirestore(uid: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = firestore.collection("users").document(uid)

    // Check if the user's document exists
    userRef.get()
        .addOnSuccessListener { document ->
            if (!document.exists()) {
                // Create a new document for the user
                userRef.set(mapOf("initialized" to true))
                    .addOnSuccessListener {
                        Log.d("AuthViewModel", "Firestore initialized for user: $uid")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AuthViewModel", "Error initializing Firestore: ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            Log.e("AuthViewModel", "Error checking Firestore initialization: ${exception.message}")
        }
}

