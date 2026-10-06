package com.akshar.dyslexiareader.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CameraUiState(
    val isTorchOn: Boolean = false,
    val capturedImageUri: Uri? = null,
    val capturedBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val rotationDegrees: Float = 0f,
    val isContrastEnhanced: Boolean = false,
    val normalizedCropRect: RectF = RectF(0.05f, 0.05f, 0.95f, 0.95f),
    val isOcrProcessing: Boolean = false,
    val ocrErrorMessage: String? = null,
    val scannedDocumentId: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val ocrEngine: OcrEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchOn = !_uiState.value.isTorchOn)
    }

    fun onImageCaptured(uri: Uri) {
        viewModelScope.launch {
            val bitmap = BitmapUtil.decodeSampledBitmapFromUri(context, uri)
            _uiState.value = _uiState.value.copy(
                capturedImageUri = uri,
                capturedBitmap = bitmap,
                processedBitmap = bitmap,
                rotationDegrees = 0f,
                isContrastEnhanced = false,
                ocrErrorMessage = null,
                scannedDocumentId = null
            )
        }
    }

    fun rotateImage() {
        val currentBitmap = _uiState.value.capturedBitmap ?: return
        val newRotation = (_uiState.value.rotationDegrees + 90f) % 360f
        val rotated = BitmapUtil.rotateBitmap(currentBitmap, newRotation)
        val finalBitmap = if (_uiState.value.isContrastEnhanced) {
            BitmapUtil.enhanceTextContrast(rotated)
        } else {
            rotated
        }
        _uiState.value = _uiState.value.copy(
            rotationDegrees = newRotation,
            processedBitmap = finalBitmap
        )
    }

    fun toggleContrastEnhance() {
        val currentBitmap = _uiState.value.capturedBitmap ?: return
        val willEnhance = !_uiState.value.isContrastEnhanced
        val rotated = BitmapUtil.rotateBitmap(currentBitmap, _uiState.value.rotationDegrees)
        val finalBitmap = if (willEnhance) {
            BitmapUtil.enhanceTextContrast(rotated)
        } else {
            rotated
        }
        _uiState.value = _uiState.value.copy(
            isContrastEnhanced = willEnhance,
            processedBitmap = finalBitmap
        )
    }

    fun updateCropRect(rect: RectF) {
        _uiState.value = _uiState.value.copy(normalizedCropRect = rect)
    }

    fun runOcrOnCroppedImage(onSuccess: (String) -> Unit) {
        val bitmap = _uiState.value.processedBitmap ?: return
        val cropRect = _uiState.value.normalizedCropRect

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOcrProcessing = true, ocrErrorMessage = null)
            try {
                val cropped = BitmapUtil.cropBitmap(bitmap, cropRect)
                val ocrResult = ocrEngine.recognizeText(cropped)

                if (ocrResult.text.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isOcrProcessing = false,
                        ocrErrorMessage = "We couldn't find clear text on that page. Let's try taking another photo in brighter light."
                    )
                    return@launch
                }

                val parsed = UnicodeIndicTextUtil.parseReaderText(ocrResult.text)
                val settings = preferencesRepository.userSettingsFlow.first()
                val profileId = settings.activeProfileId ?: "default_profile"
                val docId = UUID.randomUUID().toString()

                // Generate clean, readable title from first few words or default
                val firstWords = parsed.allWords.take(5).joinToString(" ") { it.word }
                val title = if (firstWords.isNotBlank()) firstWords else "Scanned Page"

                val file = BitmapUtil.saveBitmapToLocalCache(context, cropped, "doc_$docId.jpg")

                val document = DocumentEntity(
                    id = docId,
                    childProfileId = profileId,
                    title = title,
                    rawOcrText = ocrResult.text,
                    cleanText = parsed.fullText,
                    detectedScript = parsed.script.name,
                    imagePath = file.absolutePath,
                    wordCount = parsed.allWords.size,
                    readingProgressPercent = 0,
                    lastReadWordIndex = 0
                )

                documentRepository.saveDocument(document)
                _uiState.value = _uiState.value.copy(
                    isOcrProcessing = false,
                    scannedDocumentId = docId
                )
                onSuccess(docId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOcrProcessing = false,
                    ocrErrorMessage = "That photo was a bit blurry. Let's try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState()
    }
}
