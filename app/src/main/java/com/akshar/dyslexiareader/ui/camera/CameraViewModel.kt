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
import com.akshar.dyslexiareader.domain.util.TextCleanupUtil
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

data class ScannedPage(
    val pageNumber: Int,
    val bitmap: Bitmap,
    val extractedText: String
)

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
    val scannedDocumentId: String? = null,
    val scannedPages: List<ScannedPage> = emptyList(),
    val currentPageIndex: Int = 1
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

    /**
     * Runs OCR on the current page and saves the complete document (single or multi-page).
     */
    fun runOcrAndFinish(onSuccess: (String) -> Unit) {
        val bitmap = _uiState.value.processedBitmap ?: return
        val cropRect = _uiState.value.normalizedCropRect

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOcrProcessing = true, ocrErrorMessage = null)
            try {
                val cropped = BitmapUtil.cropBitmap(bitmap, cropRect)
                val ocrResult = ocrEngine.recognizeText(cropped)

                if (ocrResult.text.isBlank() && _uiState.value.scannedPages.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isOcrProcessing = false,
                        ocrErrorMessage = "We couldn't find clear text on that page. That photo was a bit blurry. Let's try again in brighter light."
                    )
                    return@launch
                }

                val currentText = ocrResult.text.ifBlank { "" }
                val currentPage = ScannedPage(
                    pageNumber = _uiState.value.scannedPages.size + 1,
                    bitmap = cropped,
                    extractedText = currentText
                )

                val allPages = _uiState.value.scannedPages + listOf(currentPage)

                // Combine text from all pages
                val combinedRawText = allPages.joinToString("\n\n--- Page Break ---\n\n") { it.extractedText }
                val cleanedCombinedText = TextCleanupUtil.cleanupOcrText(combinedRawText)

                val parsed = UnicodeIndicTextUtil.parseReaderText(cleanedCombinedText)
                val settings = preferencesRepository.userSettingsFlow.first()
                val profileId = settings.activeProfileId ?: "default_profile"
                val docId = UUID.randomUUID().toString()

                // Generate title from first words of the document
                val firstWords = parsed.allWords.take(5).joinToString(" ") { it.word }
                val title = if (firstWords.isNotBlank()) firstWords else "Textbook Lesson"

                // Save first page image preview in persistent app-private internal storage
                val file = BitmapUtil.saveBitmapToAppPrivateStorage(context, allPages.first().bitmap, "doc_$docId.jpg")

                val document = DocumentEntity(
                    id = docId,
                    childProfileId = profileId,
                    title = title,
                    rawOcrText = combinedRawText,
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
                    scannedDocumentId = docId,
                    scannedPages = emptyList()
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

    /**
     * Adds the current cropped page to the document and prepares for scanning another page.
     */
    fun addCurrentPageAndScanAnother(onNavigateToCamera: () -> Unit) {
        val bitmap = _uiState.value.processedBitmap ?: return
        val cropRect = _uiState.value.normalizedCropRect

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOcrProcessing = true, ocrErrorMessage = null)
            try {
                val cropped = BitmapUtil.cropBitmap(bitmap, cropRect)
                val ocrResult = ocrEngine.recognizeText(cropped)

                val page = ScannedPage(
                    pageNumber = _uiState.value.scannedPages.size + 1,
                    bitmap = cropped,
                    extractedText = ocrResult.text
                )

                _uiState.value = _uiState.value.copy(
                    scannedPages = _uiState.value.scannedPages + listOf(page),
                    currentPageIndex = _uiState.value.scannedPages.size + 2,
                    capturedBitmap = null,
                    processedBitmap = null,
                    capturedImageUri = null,
                    isOcrProcessing = false,
                    rotationDegrees = 0f,
                    isContrastEnhanced = false
                )
                onNavigateToCamera()
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
