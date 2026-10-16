package com.akshar.dyslexiareader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.data.sync.SyncManager
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class LibraryTab {
    RECENT,
    FAVORITES,
    SUBJECTS
}

data class LibraryUiState(
    val searchQuery: String = "",
    val activeTab: LibraryTab = LibraryTab.RECENT,
    val selectedSubject: String? = null,
    val selectedTagFilter: String? = null,
    val selectedScriptFilter: String? = null,
    val documentToRename: DocumentEntity? = null,
    val documentToEditTags: DocumentEntity? = null,
    val documentToDelete: DocumentEntity? = null,
    val isClearAllHistoryDialogOpen: Boolean = false,
    val recentlyDeletedDocumentId: String? = null,
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    val syncStatus = syncManager.syncStatus

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val seededProfileIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            preferencesRepository.userSettingsFlow.collect { settings ->
                val profileId = settings.activeProfileId
                if (!profileId.isNullOrBlank() && !seededProfileIds.contains(profileId)) {
                    seededProfileIds.add(profileId)
                    val existing = documentRepository.getDocumentsForProfile(profileId).first()
                    if (existing.isEmpty()) {
                        seedSampleLessons(profileId)
                    }
                }
            }
        }
    }

    private suspend fun seedSampleLessons(profileId: String) {
        val engText = "The solar system consists of the Sun and eight planets orbiting around it. Earth is our home planet where living things grow and thrive. The moon shines brightly in the quiet night sky."
        val parsedEng = UnicodeIndicTextUtil.parseReaderText(engText)
        val engDoc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            title = "The Solar System and Earth",
            rawOcrText = engText,
            cleanText = parsedEng.fullText,
            detectedScript = "LATIN",
            subject = "Science",
            tags = "Science, Space",
            wordCount = parsedEng.allWords.size
        )

        val hindiText = "एक छोटा तारा रात के सुंदर आकाश में चमकता था। उसने देखा कि सुबह होते ही सूरज अपनी सुनहरी किरणें फैलाता है। चिड़ियाँ चहकने लगती हैं और फूल खिलते हैं।"
        val parsedHindi = UnicodeIndicTextUtil.parseReaderText(hindiText)
        val hindiDoc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            title = "नन्हा तारा और सूरज",
            rawOcrText = hindiText,
            cleanText = parsedHindi.fullText,
            detectedScript = "DEVANAGARI",
            subject = "Hindi",
            tags = "Hindi, Story",
            wordCount = parsedHindi.allWords.size
        )

        documentRepository.saveDocument(engDoc)
        documentRepository.saveDocument(hindiDoc)
    }

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
                // Tab Filter
                val matchesTab = when (state.activeTab) {
                    LibraryTab.RECENT -> true
                    LibraryTab.FAVORITES -> doc.isFavorite
                    LibraryTab.SUBJECTS -> state.selectedSubject == null || doc.subject.equals(state.selectedSubject, ignoreCase = true)
                }

                // Search Query Filter
                val matchesQuery = state.searchQuery.isBlank() ||
                    doc.title.contains(state.searchQuery, ignoreCase = true) ||
                    doc.cleanText.contains(state.searchQuery, ignoreCase = true) ||
                    doc.tags.contains(state.searchQuery, ignoreCase = true)

                // Script Filter
                val matchesScript = state.selectedScriptFilter == null ||
                    doc.detectedScript.equals(state.selectedScriptFilter, ignoreCase = true)

                // Tag Filter
                val matchesTag = state.selectedTagFilter == null ||
                    doc.tags.split(",").map { it.trim() }.contains(state.selectedTagFilter)

                matchesTab && matchesQuery && matchesScript && matchesTag
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTabSelected(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun onSubjectSelected(subject: String?) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }

    fun onTagFilterSelected(tag: String?) {
        _uiState.value = _uiState.value.copy(selectedTagFilter = tag)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onScriptFilterSelected(script: String?) {
        _uiState.value = _uiState.value.copy(selectedScriptFilter = script)
    }

    fun toggleFavorite(document: DocumentEntity) {
        viewModelScope.launch {
            documentRepository.toggleFavorite(document.id, !document.isFavorite)
        }
    }

    fun requestRenameDocument(document: DocumentEntity) {
        _uiState.value = _uiState.value.copy(documentToRename = document)
    }

    fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(documentToRename = null)
    }

    fun confirmRenameDocument(newTitle: String) {
        val doc = _uiState.value.documentToRename ?: return
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                documentRepository.renameDocument(doc.id, newTitle.trim())
                _uiState.value = _uiState.value.copy(documentToRename = null)
            }
        }
    }

    fun requestEditTags(document: DocumentEntity) {
        _uiState.value = _uiState.value.copy(documentToEditTags = document)
    }

    fun dismissEditTagsDialog() {
        _uiState.value = _uiState.value.copy(documentToEditTags = null)
    }

    fun confirmEditTags(newTags: String) {
        val doc = _uiState.value.documentToEditTags ?: return
        viewModelScope.launch {
            documentRepository.updateTags(doc.id, newTags.trim())
            _uiState.value = _uiState.value.copy(documentToEditTags = null)
        }
    }

    fun requestDeleteDocument(document: DocumentEntity) {
        _uiState.value = _uiState.value.copy(documentToDelete = document)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(documentToDelete = null)
    }

    fun confirmSoftDeleteDocument() {
        val doc = _uiState.value.documentToDelete ?: return
        viewModelScope.launch {
            documentRepository.softDeleteDocument(doc.id)
            _uiState.value = _uiState.value.copy(
                documentToDelete = null,
                recentlyDeletedDocumentId = doc.id,
                snackbarMessage = "\"${doc.title}\" deleted"
            )
        }
    }

    fun undoDelete() {
        val docId = _uiState.value.recentlyDeletedDocumentId ?: return
        viewModelScope.launch {
            documentRepository.restoreDocument(docId)
            _uiState.value = _uiState.value.copy(
                recentlyDeletedDocumentId = null,
                snackbarMessage = "Restored lesson"
            )
        }
    }

    fun openClearAllHistoryDialog() {
        _uiState.value = _uiState.value.copy(isClearAllHistoryDialogOpen = true)
    }

    fun dismissClearAllHistoryDialog() {
        _uiState.value = _uiState.value.copy(isClearAllHistoryDialogOpen = false)
    }

    fun confirmClearAllHistory() {
        viewModelScope.launch {
            val userSettings = preferencesRepository.userSettingsFlow.first()
            val profileId = userSettings.activeProfileId ?: "default"
            documentRepository.deleteAllForProfile(profileId)
            _uiState.value = _uiState.value.copy(
                isClearAllHistoryDialogOpen = false,
                snackbarMessage = "Reading history cleared"
            )
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
