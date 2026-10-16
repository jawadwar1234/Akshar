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
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

val SUBJECT_LIST = listOf("All", "Science", "Hindi", "Marathi", "Math", "Social Studies", "English", "General")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onDocumentSelected: (String) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToStats: () -> Unit = {}
) {
    val documents by viewModel.documents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (uiState.recentlyDeletedDocumentId != null) "Undo" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.nav_library),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        val syncLabel = when (syncStatus) {
                            com.akshar.dyslexiareader.data.sync.SyncStatus.SAVED_TO_CLOUD -> "● Saved to your account"
                            com.akshar.dyslexiareader.data.sync.SyncStatus.SYNCING -> "● Syncing..."
                            com.akshar.dyslexiareader.data.sync.SyncStatus.OFFLINE_PAUSED -> "● Saved on this phone (Offline)"
                            else -> "● Saved on this phone"
                        }
                        Text(
                            text = syncLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (syncStatus == com.akshar.dyslexiareader.data.sync.SyncStatus.SAVED_TO_CLOUD) CalmTeal else CharcoalTextSecondary
                        )
                    }
                },
                actions = {
                    // Clear all history button if documents exist
                    if (documents.isNotEmpty()) {
                        IconButton(onClick = viewModel::openClearAllHistoryDialog) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All History",
                                tint = CharcoalTextSecondary
                            )
                        }
                    }

                    // Insights / Stats Button
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Progress & Insights",
                            tint = TerracottaAccent
                        )
                    }

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
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = onNavigateToScan,
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text(stringResource(R.string.nav_scan), fontWeight = FontWeight.Bold) },
                containerColor = TerracottaAccent,
                contentColor = Color.White
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PaperCreamBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DesignTokens.Spacing.lg),
            horizontalAlignment = Alignment.Start
        ) {
            // Main Tabs: Recent, Favorites, Subjects
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                containerColor = PaperCreamBackground,
                contentColor = TerracottaAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                        color = TerracottaAccent
                    )
                }
            ) {
                Tab(
                    selected = uiState.activeTab == LibraryTab.RECENT,
                    onClick = { viewModel.onTabSelected(LibraryTab.RECENT) },
                    text = { Text("Recent", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.activeTab == LibraryTab.FAVORITES,
                    onClick = { viewModel.onTabSelected(LibraryTab.FAVORITES) },
                    text = { Text("Favourites", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.activeTab == LibraryTab.SUBJECTS,
                    onClick = { viewModel.onTabSelected(LibraryTab.SUBJECTS) },
                    text = { Text("Subjects", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

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

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

            // Filter Chips (Subjects row if in SUBJECTS tab, else Script Filters)
            if (uiState.activeTab == LibraryTab.SUBJECTS) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(SUBJECT_LIST) { subject ->
                        val isSelected = (subject == "All" && uiState.selectedSubject == null) ||
                            (uiState.selectedSubject.equals(subject, ignoreCase = true))
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSubjectSelected(if (subject == "All") null else subject) },
                            label = { Text(subject) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CalmTeal.copy(alpha = 0.15f),
                                selectedLabelColor = CalmTeal
                            )
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

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
                    items(documents, key = { it.id }) { doc ->
                        DocumentCard(
                            document = doc,
                            onClick = { onDocumentSelected(doc.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(doc) },
                            onRename = { viewModel.requestRenameDocument(doc) },
                            onEditTags = { viewModel.requestEditTags(doc) },
                            onDelete = { viewModel.requestDeleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    // Rename Document Dialog
    if (uiState.documentToRename != null) {
        var newTitle by remember { mutableStateOf(uiState.documentToRename!!.title) }
        AlertDialog(
            onDismissRequest = viewModel::dismissRenameDialog,
            title = { Text("Rename Lesson", color = CharcoalTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Lesson Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRenameDocument(newTitle) },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                ) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissRenameDialog) {
                    Text("Cancel")
                }
            },
            containerColor = PaperCreamBackground
        )
    }

    // Edit Tags Dialog
    if (uiState.documentToEditTags != null) {
        var tagsText by remember { mutableStateOf(uiState.documentToEditTags!!.tags) }
        AlertDialog(
            onDismissRequest = viewModel::dismissEditTagsDialog,
            title = { Text("Edit Tags", color = CharcoalTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter comma-separated tags (e.g. Science, Exam):", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        placeholder = { Text("Chapter 1, Homework") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmEditTags(tagsText) },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmTeal)
                ) {
                    Text("Save Tags", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissEditTagsDialog) {
                    Text("Cancel")
                }
            },
            containerColor = PaperCreamBackground
        )
    }

    // Delete Confirmation Dialog
    if (uiState.documentToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Delete Scanned Lesson", color = CharcoalTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.library_btn_delete_confirm), color = CharcoalTextSecondary) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmSoftDeleteDocument,
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

    // Clear All History Confirmation Dialog
    if (uiState.isClearAllHistoryDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAllHistoryDialog,
            title = { Text("Clear All Reading History", color = CharcoalTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all reading history and lessons for this profile? This will clear your list.", color = CharcoalTextSecondary) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmClearAllHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = GentleErrorMuted)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissClearAllHistoryDialog) {
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
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onEditTags: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = remember(document.createdAt) { dateFormat.format(Date(document.createdAt)) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignTokens.ElevationAndBorders.borderThin,
                color = PaperCreamBorder,
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md)
        ) {
            // Header Row: Title + Favorite + Rename + Tags + Delete
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (document.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (document.isFavorite) TerracottaAccent else CharcoalTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = "Rename",
                            tint = CharcoalTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onEditTags, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Edit Tags",
                            tint = CharcoalTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CharcoalTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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

            // Metadata row: Script badge, Subject, word count, date
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
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Box(
                        modifier = Modifier
                            .background(TerracottaAccent.copy(alpha = 0.10f), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = document.subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = TerracottaAccent
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

            // Tags row if present
            if (document.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "Tags: ${document.tags}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalTextSecondary.copy(alpha = 0.8f)
                )
            }

            // Reading Progress Bar & Resume Position indicator
            if (document.readingProgressPercent > 0) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { document.readingProgressPercent / 100f },
                        modifier = Modifier.weight(1f).height(4.dp),
                        color = TerracottaAccent,
                        trackColor = PaperCreamBorder
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                    Text(
                        text = "${document.readingProgressPercent}% read",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaAccent
                    )
                }
            }
        }
    }
}
