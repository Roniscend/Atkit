package com.example.atkit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.atkit.OralVisApplication
import com.example.atkit.data.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as OralVisApplication).repository

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _capturedImageCount = MutableStateFlow(0)
    val capturedImageCount: StateFlow<Int> = _capturedImageCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allSessions: Flow<List<SessionEntity>> = repository.getAllSessions()

    val searchResults: Flow<List<SessionEntity>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchSessions(query)
        }
    }

    fun startNewSession(sessionId: String) {
        _currentSessionId.value = sessionId
        _capturedImageCount.value = 0
    }

    fun incrementImageCount() {
        _capturedImageCount.value = _capturedImageCount.value + 1
    }

    fun endSession(name: String, age: Int) {
        viewModelScope.launch {
            _currentSessionId.value?.let { sessionId ->
                val session = SessionEntity(
                    sessionId = sessionId,
                    name = name,
                    age = age,
                    timestamp = System.currentTimeMillis(),
                    imageCount = _capturedImageCount.value
                )
                repository.insertSession(session)
                _currentSessionId.value = null
                _capturedImageCount.value = 0
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }


    suspend fun getSessionById(sessionId: String): SessionEntity? {
        return repository.getSessionById(sessionId)
    }
}