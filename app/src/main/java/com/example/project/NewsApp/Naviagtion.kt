package com.example.project.NewsApp
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(articleViewModel: ArticleViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val newsViewModel = NewsViewModel()

    NavHost(navController, startDestination = "signIn") {
        composable("signIn") { SignInScreen(navController, authViewModel) }
        composable("signUp") { SignUpScreen(navController, authViewModel) }

        composable("home") { frontScreen(navController, newsViewModel, authViewModel, articleViewModel) }
        composable("forgotPassword") { ForgotPasswordScreen(navController, authViewModel) }

        composable("articleDetail/{url}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")
            if (url != null) {
                DetailedScreen(url)
            }
        }

        // New categorySpecific route
        composable("categorySpecific/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            NewsArticlesScreen(category = category, navController = navController, viewModel = newsViewModel)
        }

        composable("savedArticles") {
            SaveArticles(navController, authViewModel, articleViewModel)
        }

        composable("category") {
            CategoryScreen(navController)
        }
    }
}
