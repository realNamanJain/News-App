package com.example.project.NewsApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var previousArticles = emptyList<Article>()

    companion object {
        private const val API_KEY = "85da8d54a69fe4d5cd2c61d2ae4acb7a"
    }

    init {
        loadTopHeadlines()
    }

    private fun loadTopHeadlines(country: String? = "", category: String? = null) {
        fetchNews {
            when {
                country != null && category != null -> RetrofitInstance.api.getCategoryHeadlines(category, "en", country, API_KEY)
                country != null -> RetrofitInstance.api.getTopHeadlinesIndia("en", country, API_KEY)
                else -> RetrofitInstance.api.getTopHeadlines("en", API_KEY)
            }
        }
    }

    private fun fetchNews(apiCall: suspend () -> NewsResponse) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiCall()
                Log.d("NewsViewModel", "Fetched articles: ${response.articles}")

                val newArticles = response.articles
                val updatedArticles = (previousArticles + newArticles).distinctBy { it.url }
                _articles.value = updatedArticles
                previousArticles = updatedArticles

                _errorMessage.value = null
            } catch (e: Exception) {
                if (e is retrofit2.HttpException && e.code() == 429) {
                    _errorMessage.value = "Rate limit exceeded. Please try again later."
                    // Optional: Add retry logic with a delay or exponential backoff here
                } else {
                    _errorMessage.value = e.message ?: "An error occurred while fetching the news."
                }
                Log.e("NewsViewModel", "Error fetching articles", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to fetch top headlines globally or for a specific country
    fun loadTopHeadlinesForCountry(country: String = "") {
        loadTopHeadlines(country = country)
    }

    // Function to search for articles by a specific query
    fun searchArticles(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchArticles(query,"en", API_KEY)
                _articles.value = response.articles // Update the list
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred while fetching articles."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNewsArticles(category: String) {
        loadCategoryNews(category)
    }

    // Function to load category-specific news
    private fun loadCategoryNews(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getCategoryHeadlines(category, "en", "in", API_KEY)
                _articles.value = response.articles
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error fetching news."
            } finally {
                _isLoading.value = false
            }
        }
    }

//    fun getNewsArticles(category: String) = flow {
//        _isLoading.value = true
//        try {
//            // Ensure you're calling the correct endpoint for category-specific news
//            val response = RetrofitInstance.api.getCategoryHeadlines(category, "en", "in", API_KEY)
//            // Emit the fetched articles as flow
//            emit(response.articles)
//        } catch (e: Exception) {
//            _errorMessage.value = e.message ?: "Error fetching news by category."
//            Log.e("NewsViewModel", "Error fetching articles", e)
//        } finally {
//            _isLoading.value = false
//        }
//    }

}
