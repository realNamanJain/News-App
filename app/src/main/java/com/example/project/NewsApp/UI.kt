package com.example.project.NewsApp

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.webkit.WebViewClientCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun frontScreen(navController: NavController, viewModel: NewsViewModel = viewModel(), authViewModel: AuthViewModel, articleViewModel: ArticleViewModel = viewModel()) {
    val username = authViewModel.getCurrentUser()
    val articles by viewModel.articles.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var isSignOutLoading by remember { mutableStateOf(false) } // To handle loading state for sign out
    var signOutErrorMessage by remember { mutableStateOf<String?>(null) } // To handle error message for sign out

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(10000)
            viewModel.searchArticles(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taaza Khabar") },
                actions = {
                    IconButton(onClick = {
                        isSignOutLoading = true
                        authViewModel.signOut(
                            onSuccess = {
                                isSignOutLoading = false
                                navController.navigate("signIn") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onFailure = { errorMessage ->
                                isSignOutLoading = false
                                signOutErrorMessage = errorMessage
                            }
                        )
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedIndex, navController) { index ->
                selectedIndex = index
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search news...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank() && !isLoading) {
                            viewModel.searchArticles(searchQuery)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )

            if (errorMessage?.isNotEmpty() == true) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* Retry or handle error */ }) {
                            Text("Retry")
                        }
                    }
                ) { Text(text = errorMessage!!) }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(articles) { article ->
                        newsArticles(
                            article,
                            onArticleClick = { url -> navController.navigate("articleDetail/${encodeUrl(url)}") },
                            articleViewModel, authViewModel
                        )
                    }
                }
            }

            // Show sign-out error message if it exists
            signOutErrorMessage?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { signOutErrorMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                ) { Text(text = it) }
            }

            // Show loading spinner while signing out
            if (isSignOutLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}


@Composable
fun BottomNavigationBar(selectedIndex: Int, navController: NavController, onItemSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = {
                onItemSelected(0)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = {
                onItemSelected(1)
                navController.navigate("category")
            },
            icon = { Icon(Icons.Default.Menu, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = {
                onItemSelected(2)
                navController.navigate("savedArticles")
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}


fun encodeUrl(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())

@Composable
fun newsArticles(article: Article, onArticleClick: (String) -> Unit, articleViewModel: ArticleViewModel, authViewModel: AuthViewModel){
    val username = authViewModel.getCurrentUser()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onArticleClick(article.url) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(article.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = { articleViewModel.addArticle(article) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Save Article")
            }
        }
    }
}

@Composable
fun DetailedScreen(encodedUrl: String){
    val url = java.net.URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize()) {
            it.webViewClient = WebViewClientCompat()
            it.loadUrl(url)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveArticles(
    navController: NavController,
    authViewModel: AuthViewModel,
    articleViewModel: ArticleViewModel
) {
    val username = authViewModel.getCurrentUser()
    val savedArticles by articleViewModel.articles.collectAsState()
    val isLoading by articleViewModel.isLoading.collectAsState()
    val errorMessage by articleViewModel.errorMessage.collectAsState()
    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        articleViewModel.fetchArticles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Articles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Home, contentDescription = "Back to Home")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedIndex, navController) { index ->
                selectedIndex = index
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Show loading spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (!errorMessage.isNullOrEmpty()) {
                // Show error message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage ?: "Something went wrong.")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { articleViewModel.fetchArticles() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (savedArticles.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No saved articles yet.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Start exploring and save your favorite articles!")
                    }
                }
            } else {
                // Show saved articles list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(savedArticles) { article ->
                        newsArticles(
                            article = article,
                            onArticleClick = { url ->
                                navController.navigate("articleDetail/${encodeUrl(url)}")
                            },
                            articleViewModel = articleViewModel,
                            authViewModel = authViewModel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(navController: NavController, viewModel: NewsViewModel = viewModel()) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text(
                text = "Get news on these categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Categories grid
            Column {
                CategoryRow(
                    icons = listOf(
                        R.drawable.growth to "business",
                        R.drawable.health to "health"
                    ),
                    navController
                )
                Spacer(modifier = Modifier.height(16.dp))
                CategoryRow(
                    icons = listOf(
                        R.drawable.science to "science",
                        R.drawable.running to "sports"
                    ),
                    navController
                )
                Spacer(modifier = Modifier.height(16.dp))
                CategoryRow(
                    icons = listOf(
                        R.drawable.technology to "technology",
                        R.drawable.video to "entertainment"
                    ),
                    navController
                )
            }
        }
    }
}

@Composable
fun CategoryRow(icons: List<Pair<Int, String>>, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        icons.forEach { (iconRes, category) ->
            Box(
                modifier = Modifier
                    .weight(1f) // Make each icon take up equal space in the row
                    .padding(8.dp)
                    .clickable {
                        // Navigate to the NewsArticlesScreen for the selected category
                        navController.navigate("categorySpecific/$category")
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = category,
                        modifier = Modifier
                            .size(100.dp) // Increased icon size
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(16.dp) // Adjust padding to give more breathing room
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp, // Increased font size
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsArticlesScreen(category: String, navController: NavController, viewModel: NewsViewModel = viewModel()) {
    val articles by viewModel.articles.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(category) {
        viewModel.getNewsArticles(category)  // Fetch news for the specific category
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = category.replaceFirstChar { it.uppercase() } + " News") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 32.dp)
                        )
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    articles.isNotEmpty() -> {
                        LazyColumn {
                            items(articles) { article ->
                                CategoryNewsArticleItem(article = article) { url ->
                                    navController.navigate("articleDetail/${encodeUrl(url)}")
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "No articles available",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
@Composable
fun CategoryNewsArticleItem(article: Article, onArticleClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onArticleClick(article.url) } // Make the entire item clickable
    ) {
        // Display image if available
        article.imageUrl?.let {
            val painter = rememberAsyncImagePainter(it)
            Image(
                painter = painter,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)) // Rounded corners for the image
                    .padding(bottom = 8.dp)
            )
        }

        // Title of the article
        Text(
            text = article.title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp) // Padding on left and right for text
                .padding(bottom = 4.dp)
        )

        // Description of the article
        Text(
            text = article.description ?: "Description not available",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier
                .padding(horizontal = 16.dp) // Padding for description text
                .padding(bottom = 16.dp) // Bottom padding to create space between items
        )
    }
}
