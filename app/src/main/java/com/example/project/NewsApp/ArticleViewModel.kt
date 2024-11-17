package com.example.project.NewsApp

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ViewModel to handle the business logic of articles (fetch, add, etc.)
class ArticleViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val articleRepository = ArticleRepository()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> get() = _articles

    private val _isArticleAdded = MutableStateFlow(false)
    val isArticleAdded: StateFlow<Boolean> get() = _isArticleAdded

    private val _specificArticle = MutableStateFlow<Article?>(null)
    val specificArticle: StateFlow<Article?> get() = _specificArticle

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    fun addArticle(article: Article) {
        val uid = authViewModel.getCurrentUser()
        if (uid != null) {
            articleRepository.addArticle(uid, article, {
                _isArticleAdded.value = true
                _isArticleAdded.value = false
            }, { exception ->
                Log.e("ArticleViewModel", "Error adding article: ${exception.message}", exception)
            })
        } else {
            Log.e("ArticleViewModel", "Cannot add article: User UID is null")
        }
    }

    fun fetchArticles() {
        val uid = authViewModel.getCurrentUser()
        if (uid != null) {
            _isLoading.value = true
            articleRepository.fetchArticles(uid, { fetchedArticles ->
                _articles.value = fetchedArticles
                _isLoading.value = false
                _errorMessage.value = null
            }, { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error fetching articles: ${exception.message}"
                Log.e("ArticleViewModel", "Error fetching articles", exception)
            })
        } else {
            _errorMessage.value = "Cannot fetch articles: User UID is null"
        }
    }

    fun fetchSpecificArticle(name: String, articleTitle: String) {
        articleRepository.fetchSpecificArticle(name, articleTitle, { fetchedArticle ->
            _specificArticle.value = fetchedArticle
        }, { exception ->
            Log.e("ArticleViewModel", "Error fetching specific article: ${exception.message}", exception)
        })
    }
}
