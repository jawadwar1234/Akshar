package com.akshar.dyslexiareader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val searchQuery: String = "",
    val selectedScriptFilter: String? = null,
    val documentToDelete: DocumentEntity? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    val activeProfileId: StateFlow<String?> = preferencesRepository.userSettingsFlow
        .combine(_uiState) { settings, _ -> settings.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val documents: StateFlow<List<DocumentEntity>> = preferencesRepository.userSettingsFlow
        .flatMapLatest { settings ->
            val profileId = settings.activeProfileId ?: "default"
            documentRepository.getDocumentsForProfile(profileId)
        }
        .combine(_uiState) { docs, state ->
            docs.filter { doc ->
                val matchesQuery = state.searchQuery.isBlank() ||
                    doc.title.contains(state.searchQuery, ignoreCase = true) ||
                    doc.cleanText.contains(state.searchQuery, ignoreCase = true)

                val matchesScript = state.selectedScriptFilter == null ||
                    doc.detectedScript.equals(state.selectedScriptFilter, ignoreCase = true)

                matchesQuery && matchesScript
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onScriptFilterSelected(script: String?) {
        _uiState.value = _uiState.value.copy(selectedScriptFilter = script)
    }

    fun requestDeleteDocument(document: DocumentEntity) {
        _uiState.value = _uiState.value.copy(documentToDelete = document)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(documentToDelete = null)
    }

    fun confirmDeleteDocument() {
        val doc = _uiState.value.documentToDelete ?: return
        viewModelScope.launch {
            documentRepository.deleteDocument(doc.id)
            _uiState.value = _uiState.value.copy(documentToDelete = null)
        }
    }
}
