package com.example.project.NewsApp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName

// Data class to hold the response from the news API
data class NewsResponse(
    val totalArticles: Int,   // Total number of articles returned
    val articles: List<Article>  // List of articles
)

// Data class to represent an article's data
data class Article(
    val title: String = "",  // Default empty string for title
    val description: String = "",  // Default empty string for description
    val content: String = "",  // Default empty string for content
    val url: String = "",  // Default empty string for URL
    @SerializedName("image") val imageUrl: String? = null,  // Image URL, can be null
    val publishedAt: String = "",  // Default empty string for published date
    val source: Source = Source()  // Source of the article, initialized with a default Source object
)

// Data class for the source of the article, including its name and URL
data class Source(
    val name: String = "",  // Default empty string for source name
    val url: String = ""    // Default empty string for source URL
)

// Repository class to manage data operations (add, fetch) related to articles
class ArticleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    // Adds a new article to Firestore for a specific user
    fun addArticle(
        uid: String,
        article: Article,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val articleRef = firestore.collection("users")
            .document(uid)
            .collection("Favourite Articles")
            .document(article.title)

        articleRef.set(article)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun fetchArticles(
        uid: String,
        onResult: (List<Article>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .collection("Favourite Articles")
            .get()
            .addOnSuccessListener { documents ->
                val articles = documents.mapNotNull { it.toObject(Article::class.java) }
                onResult(articles)
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Fetches a specific article for a user
    fun fetchSpecificArticle(
        username: String,
        articleTitle: String,
        onResult: (Article?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(username)
            .collection("Favourite Articles")
            .document(articleTitle)
            .get()
            .addOnSuccessListener { document ->
                val article = document.toObject(Article::class.java)
                onResult(article)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
