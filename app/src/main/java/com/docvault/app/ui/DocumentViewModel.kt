package com.docvault.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.docvault.app.data.Document
import com.docvault.app.data.DocumentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val documents: StateFlow<List<Document>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query -> repository.search(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addDocument(
        uri: Uri,
        originalFileName: String,
        customName: String,
        tag: String,
        fileType: String
    ) {
        viewModelScope.launch {
            repository.addDocument(uri, originalFileName, customName, tag, fileType)
        }
    }

    fun renameDocument(document: Document, newName: String, newTag: String) {
        viewModelScope.launch {
            repository.renameDocument(document, newName, newTag)
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }
}
