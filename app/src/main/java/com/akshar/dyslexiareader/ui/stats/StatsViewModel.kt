package com.akshar.dyslexiareader.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.model.StudentProgressStats
import com.akshar.dyslexiareader.domain.repository.ChildProfileRepository
import com.akshar.dyslexiareader.domain.repository.StatsRepository
import com.akshar.dyslexiareader.domain.util.ProgressReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class StatsUiState(
    val activeProfileId: String? = null,
    val allProfiles: List<ChildProfileEntity> = emptyList(),
    val stats: StudentProgressStats? = null,
    val isLoading: Boolean = false,
    val isAddNoteDialogOpen: Boolean = false,
    val exportMessage: String? = null,
    val exportedPdfFile: File? = null,
    val exportedText: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val statsRepository: StatsRepository,
    private val profileRepository: ChildProfileRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadStats(initialProfileId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val profiles = profileRepository.getAllProfiles().first().filter { !it.isDeleted }
            val activeId = if (!initialProfileId.isNullOrBlank()) {
                initialProfileId
            } else {
                preferencesRepository.userSettingsFlow.first().activeProfileId
                    ?: profiles.firstOrNull()?.id
            }

            _uiState.value = _uiState.value.copy(
                allProfiles = profiles,
                activeProfileId = activeId
            )

            if (activeId != null) {
                observeStudentProgress(activeId)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectProfile(profileId: String) {
        _uiState.value = _uiState.value.copy(activeProfileId = profileId, isLoading = true)
        observeStudentProgress(profileId)
    }

    private fun observeStudentProgress(profileId: String) {
        viewModelScope.launch {
            statsRepository.getStudentProgress(profileId).collect { stats ->
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    isLoading = false
                )
            }
        }
    }

    fun openAddNoteDialog() {
        _uiState.value = _uiState.value.copy(isAddNoteDialogOpen = true)
    }

    fun closeAddNoteDialog() {
        _uiState.value = _uiState.value.copy(isAddNoteDialogOpen = false)
    }

    fun saveTeacherNote(text: String, category: String = "Observation", author: String = "Teacher") {
        val profileId = _uiState.value.activeProfileId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            statsRepository.addTeacherNote(
                profileId = profileId,
                noteText = text.trim(),
                category = category,
                authorName = author
            )
            _uiState.value = _uiState.value.copy(
                isAddNoteDialogOpen = false,
                exportMessage = "Teacher note saved successfully!"
            )
        }
    }

    fun deleteTeacherNote(noteId: String) {
        viewModelScope.launch {
            statsRepository.deleteTeacherNote(noteId)
        }
    }

    fun exportAsPdf(onPdfReady: (File) -> Unit) {
        val stats = _uiState.value.stats ?: return
        viewModelScope.launch {
            val file = ProgressReportExporter.exportProgressAsPdf(context, stats)
            if (file != null) {
                _uiState.value = _uiState.value.copy(
                    exportedPdfFile = file,
                    exportMessage = "PDF Report generated: ${file.name}"
                )
                onPdfReady(file)
            } else {
                _uiState.value = _uiState.value.copy(exportMessage = "Unable to generate report.")
            }
        }
    }

    fun exportAsText(onTextReady: (String) -> Unit) {
        val stats = _uiState.value.stats ?: return
        val text = ProgressReportExporter.exportProgressAsText(stats)
        _uiState.value = _uiState.value.copy(
            exportedText = text,
            exportMessage = "Plain text report generated."
        )
        onTextReady(text)
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }
}
