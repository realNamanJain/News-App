package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.project.NewsApp.AppNavigation
import com.example.project.NewsApp.ArticleViewModel
import com.example.project.NewsApp.AuthViewModel
import com.example.project.ui.theme.ProjectTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authViewModel: AuthViewModel
    private lateinit var articleViewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth= FirebaseAuth.getInstance()
        authViewModel = AuthViewModel(auth) // Pass FirebaseAuth instance to AuthViewModel
        articleViewModel = ArticleViewModel(authViewModel)
        setContent {
           AppNavigation(articleViewModel, authViewModel)
        }
    }
}
