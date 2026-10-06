package com.akshar.dyslexiareader.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.repository.ChildProfileRepository
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.util.PinSecurityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfilesUiState(
    val selectedProfile: ChildProfileEntity? = null,
    val unlockingProfile: ChildProfileEntity? = null,
    val enteredNumericPin: String = "",
    val enteredPicturePinIcons: List<String> = emptyList(),
    val pinErrorMessage: String? = null,
    val isUnlockSuccess: Boolean = false,
    val isAddProfileDialogOpen: Boolean = false,
    val exportStatusMessage: String? = null
)

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ChildProfileRepository,
    private val documentRepository: DocumentRepository,
    private val wordMasteryRepository: WordMasteryRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val allProfiles: StateFlow<List<ChildProfileEntity>> = profileRepository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    fun selectProfileToUnlock(profile: ChildProfileEntity) {
        _uiState.value = _uiState.value.copy(
            unlockingProfile = profile,
            enteredNumericPin = "",
            enteredPicturePinIcons = emptyList(),
            pinErrorMessage = null,
            isUnlockSuccess = false
        )
    }

    fun dismissUnlock() {
        _uiState.value = _uiState.value.copy(
            unlockingProfile = null,
            enteredNumericPin = "",
            enteredPicturePinIcons = emptyList(),
            pinErrorMessage = null
        )
    }

    fun onNumericPinDigit(digit: String) {
        val current = _uiState.value.enteredNumericPin
        if (current.length < 4) {
            val updated = current + digit
            _uiState.value = _uiState.value.copy(enteredNumericPin = updated, pinErrorMessage = null)
            if (updated.length == 4) {
                verifyNumericPin(updated)
            }
        }
    }

    fun onNumericPinBackspace() {
        val current = _uiState.value.enteredNumericPin
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredNumericPin = current.dropLast(1),
                pinErrorMessage = null
            )
        }
    }

    fun onPicturePinIconSelected(iconId: String) {
        val current = _uiState.value.enteredPicturePinIcons
        if (current.size < 3) {
            val updated = current + iconId
            _uiState.value = _uiState.value.copy(
                enteredPicturePinIcons = updated,
                pinErrorMessage = null
            )
            if (updated.size == 3) {
                verifyPicturePin(updated)
            }
        }
    }

    fun clearPicturePin() {
        _uiState.value = _uiState.value.copy(
            enteredPicturePinIcons = emptyList(),
            pinErrorMessage = null
        )
    }

    private fun verifyNumericPin(pin: String) {
        val profile = _uiState.value.unlockingProfile ?: return
        val isMatch = PinSecurityUtil.verifyPin(pin, profile.hashedPin, profile.salt)
        if (isMatch) {
            activateProfile(profile)
        } else {
            _uiState.value = _uiState.value.copy(
                enteredNumericPin = "",
                pinErrorMessage = "That PIN wasn't quite right. Let's try again!"
            )
        }
    }

    private fun verifyPicturePin(icons: List<String>) {
        val profile = _uiState.value.unlockingProfile ?: return
        val encodedPin = PinSecurityUtil.encodePicturePin(icons)
        val isMatch = PinSecurityUtil.verifyPin(encodedPin, profile.hashedPin, profile.salt)
        if (isMatch) {
            activateProfile(profile)
        } else {
            _uiState.value = _uiState.value.copy(
                enteredPicturePinIcons = emptyList(),
                pinErrorMessage = "That picture combination wasn't quite right. Let's try again!"
            )
        }
    }

    private fun activateProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            preferencesRepository.setActiveProfileId(profile.id)
            preferencesRepository.updateReaderPreferences(
                theme = profile.preferredThemeId,
                fontSize = profile.preferredFontSizeSp,
                lineSpacing = profile.preferredLineSpacing,
                letterSpacing = profile.preferredLetterSpacing,
                fontFamily = profile.preferredFontId,
                ttsSpeed = profile.preferredTtsSpeed,
                ttsPitch = profile.preferredTtsPitch
            )
            _uiState.value = _uiState.value.copy(
                selectedProfile = profile,
                unlockingProfile = null,
                isUnlockSuccess = true
            )
        }
    }

    fun createNewProfile(
        name: String,
        gradeLevel: String,
        pinType: String,
        rawPin: String,
        avatarName: String
    ) {
        viewModelScope.launch {
            val salt = PinSecurityUtil.generateSalt()
            val hashed = PinSecurityUtil.hashPin(rawPin, salt)
            val newProfile = ChildProfileEntity(
                id = UUID.randomUUID().toString(),
                parentAccountId = "parent_active",
                displayName = name.trim(),
                avatarIconName = avatarName,
                pinType = pinType,
                hashedPin = hashed,
                salt = salt,
                gradeLevel = gradeLevel
            )
            profileRepository.createProfile(newProfile)
            _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = false)
        }
    }

    fun openAddProfileDialog() {
        _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = true)
    }

    fun closeAddProfileDialog() {
        _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = false)
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
        }
    }

    fun exportData(profileId: String, onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = documentRepository.exportDataAsJson(profileId)
            onExportReady(json)
            _uiState.value = _uiState.value.copy(exportStatusMessage = "Data exported successfully!")
        }
    }

    fun deleteAllDataAndAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            profileRepository.deleteAllProfiles()
            preferencesRepository.clearAllPreferences()
            onComplete()
        }
    }
}
