package com.akshar.dyslexiareader.ui.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CalmTealContainer
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.GentleErrorMuted
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.ui.theme.TerracottaAccentContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageCropScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
    onOcrCompleted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bitmap = uiState.processedBitmap
    val totalScanned = uiState.scannedPages.size
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCreamBackground)
            .padding(DesignTokens.Spacing.md)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_cancel),
                    tint = CharcoalTextPrimary
                )
            }
            Text(
                text = if (totalScanned > 0) "Page ${totalScanned + 1} of Lesson" else stringResource(R.string.crop_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
        Text(
            text = stringResource(R.string.crop_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

        // Language / Script Selector with Beta Indicators
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedScript == null,
                    onClick = { viewModel.selectScript(null) },
                    label = { Text("Auto") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaAccent.copy(alpha = 0.15f),
                        selectedLabelColor = TerracottaAccent
                    )
                )
            }
            items(SupportedScript.entries) { script ->
                FilterChip(
                    selected = uiState.selectedScript == script,
                    onClick = { viewModel.selectScript(script) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(script.scriptName)
                            if (script.isTesseractBeta) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TerracottaAccentContainer
                                ) {
                                    Text(
                                        text = "Beta",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaAccent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaAccent.copy(alpha = 0.15f),
                        selectedLabelColor = TerracottaAccent
                    )
                )
            }
        }

        // Beta Accuracy Note Banner for Gujarati and Bengali
        val isBetaSelected = uiState.selectedScript == SupportedScript.GUJARATI || uiState.selectedScript == SupportedScript.BENGALI
        if (isBetaSelected) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CalmTealContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, CalmTeal.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm, vertical = DesignTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CalmTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = "Beta: Gujarati & Bengali OCR work best with flat, shadow-free photos in good lighting.",
                        fontSize = 11.5.sp,
                        color = CharcoalTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

        // Image Preview & Crop Area Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Color.Black.copy(alpha = 0.05f), shape = MaterialTheme.shapes.medium)
                .border(
                    width = DesignTokens.ElevationAndBorders.borderThin,
                    color = PaperCreamBorder,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Textbook page preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(DesignTokens.Spacing.xs)
                )

                // Crop border guide overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(
                            width = 2.dp,
                            color = TerracottaAccent,
                            shape = MaterialTheme.shapes.small
                        )
                )
            } else {
                Text(
                    text = "No image captured",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary
                )
            }

            // OCR Processing overlay
            if (uiState.isOcrProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PaperCreamBackground.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TerracottaAccent, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
                        Text(
                            text = "Reading your page...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                        Text(
                            text = "Extracting words on your phone",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            }
        }

        if (uiState.ocrErrorMessage != null) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GentleErrorMuted.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
                colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
            ) {
                Text(
                    text = uiState.ocrErrorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GentleErrorMuted,
                    modifier = Modifier.padding(DesignTokens.Spacing.sm)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Tool Buttons: Rotate & Enhance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = viewModel::rotateImage,
                modifier = Modifier
                    .weight(1f)
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.RotateRight,
                    contentDescription = null,
                    tint = CharcoalTextPrimary
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = stringResource(R.string.crop_btn_rotate),
                    style = MaterialTheme.typography.labelMedium,
                    color = CharcoalTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))

            OutlinedButton(
                onClick = viewModel::toggleContrastEnhance,
                modifier = Modifier
                    .weight(1f)
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = if (uiState.isContrastEnhanced) {
                    ButtonDefaults.outlinedButtonColors(containerColor = CalmTeal.copy(alpha = 0.15f))
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = if (uiState.isContrastEnhanced) CalmTeal else CharcoalTextPrimary
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = stringResource(R.string.crop_btn_enhance),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uiState.isContrastEnhanced) CalmTeal else CharcoalTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Action Buttons: "Add Another Page" and "Read Document"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
        ) {
            // Multi-Page: Add Another Page
            OutlinedButton(
                onClick = { viewModel.addCurrentPageAndScanAnother(onNavigateBack) },
                enabled = bitmap != null && !uiState.isOcrProcessing,
                modifier = Modifier
                    .weight(1f)
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CharcoalTextPrimary)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = "Add Page",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )
            }

            // Finish and Read
            Button(
                onClick = {
                    viewModel.runOcrAndFinish { docId ->
                        onOcrCompleted(docId)
                    }
                },
                enabled = bitmap != null && !uiState.isOcrProcessing,
                modifier = Modifier
                    .weight(1.2f)
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = if (totalScanned > 0) "Read All ($totalScanned+)" else stringResource(R.string.crop_btn_ocr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // Optional Language Download Dialog
    if (uiState.languageToDownload != null) {
        val model = uiState.languageToDownload!!
        AlertDialog(
            onDismissRequest = viewModel::dismissLanguageDownloadDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TerracottaAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download ${model.displayName} Data", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "To keep the initial app small, ${model.displayName} (${model.nativeName}) OCR data is downloaded on-demand.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                    Text(
                        text = "• Download Size: ${model.sizeMbFormatted}\n• Stored on phone for 100% offline use.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )

                    if (uiState.isDownloadingLanguage) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
                        LinearProgressIndicator(
                            progress = { uiState.downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = TerracottaAccent,
                            trackColor = PaperCreamBorder
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Downloading... ${(uiState.downloadProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = CharcoalTextSecondary
                        )
                    }

                    if (uiState.downloadErrorMessage != null) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                        Text(
                            text = uiState.downloadErrorMessage ?: "",
                            color = GentleErrorMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.downloadRequiredLanguage(model.code) },
                    enabled = !uiState.isDownloadingLanguage,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                ) {
                    Text(if (uiState.isDownloadingLanguage) "Downloading..." else "Download (${model.sizeMbFormatted})")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissLanguageDownloadDialog,
                    enabled = !uiState.isDownloadingLanguage
                ) {
                    Text("Cancel", color = CharcoalTextSecondary)
                }
            },
            containerColor = PaperCreamSurface
        )
    }
}
