package com.akshar.dyslexiareader.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.GentleErrorMuted
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onDocumentSelected: (String) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfiles: () -> Unit
) {
    val documents by viewModel.documents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_library),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                actions = {
                    // Switch Profile Button
                    OutlinedButton(
                        onClick = onNavigateToProfiles,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.sm)
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                        Text(stringResource(R.string.nav_profiles), style = MaterialTheme.typography.labelMedium, color = CalmTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperCreamBackground)
            )
        },
        containerColor = PaperCreamBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DesignTokens.Spacing.lg),
            horizontalAlignment = Alignment.Start
        ) {
            // Search Box
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.library_search_hint)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalTextSecondary)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CharcoalTextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerracottaAccent,
                    unfocusedBorderColor = PaperCreamBorder,
                    focusedContainerColor = PaperCreamSurface,
                    unfocusedContainerColor = PaperCreamSurface
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            // Script Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedScriptFilter == null,
                        onClick = { viewModel.onScriptFilterSelected(null) },
                        label = { Text(stringResource(R.string.library_filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaAccent.copy(alpha = 0.15f),
                            selectedLabelColor = TerracottaAccent
                        )
                    )
                }
                items(SupportedScript.entries) { script ->
                    FilterChip(
                        selected = uiState.selectedScriptFilter == script.name,
                        onClick = { viewModel.onScriptFilterSelected(script.name) },
                        label = { Text(script.scriptName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaAccent.copy(alpha = 0.15f),
                            selectedLabelColor = TerracottaAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // Documents List or Empty State
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(DesignTokens.Spacing.lg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = TerracottaAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
                        Text(
                            text = stringResource(R.string.library_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                        Text(
                            text = stringResource(R.string.library_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))
                        Button(
                            onClick = onNavigateToScan,
                            modifier = Modifier.height(DesignTokens.buttonHeight),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                            Text(
                                text = stringResource(R.string.nav_scan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                    contentPadding = PaddingValues(bottom = DesignTokens.Spacing.xxl),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(documents) { doc ->
                        DocumentCard(
                            document = doc,
                            onClick = { onDocumentSelected(doc.id) },
                            onDelete = { viewModel.requestDeleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.documentToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Delete Scanned Page", color = CharcoalTextPrimary) },
            text = { Text(stringResource(R.string.library_btn_delete_confirm), color = CharcoalTextSecondary) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteDocument,
                    colors = ButtonDefaults.buttonColors(containerColor = GentleErrorMuted)
                ) {
                    Text(stringResource(R.string.action_delete), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = PaperCreamBackground
        )
    }
}

@Composable
fun DocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = remember(document.createdAt) { dateFormat.format(Date(document.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignTokens.ElevationAndBorders.borderThin,
                color = PaperCreamBorder,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CharcoalTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Excerpt
            Text(
                text = document.cleanText,
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            // Metadata row: Script badge, word count, date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CalmTeal.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = document.detectedScript,
                            style = MaterialTheme.typography.labelMedium,
                            color = CalmTeal
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                    Text(
                        text = stringResource(R.string.library_words_count, document.wordCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextSecondary
                )
            }

            // Reading Progress Bar
            if (document.readingProgressPercent > 0) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                LinearProgressIndicator(
                    progress = { document.readingProgressPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = TerracottaAccent,
                    trackColor = PaperCreamBorder,
                )
            }
        }
    }
}
