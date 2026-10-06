package com.akshar.dyslexiareader.ui.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.GentleErrorMuted
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@Composable
fun ImageCropScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
    onOcrCompleted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bitmap = uiState.processedBitmap
    val totalScanned = uiState.scannedPages.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCreamBackground)
            .padding(DesignTokens.Spacing.md),
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

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Image Preview & Crop Area Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
}
