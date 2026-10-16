package com.akshar.dyslexiareader.ui.stats

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import com.akshar.dyslexiareader.domain.model.DailyReadingStat
import com.akshar.dyslexiareader.domain.model.StudentProgressStats
import com.akshar.dyslexiareader.domain.model.WeeklyPagesStat
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CalmTealContainer
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.ui.theme.TerracottaAccentContainer
import com.akshar.dyslexiareader.ui.theme.WarmForestGreen
import com.akshar.dyslexiareader.ui.theme.WarmForestGreenContainer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParentTeacherStatsScreen(
    profileId: String? = null,
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showExportOptions by remember { mutableStateOf(false) }

    LaunchedEffect(profileId) {
        viewModel.loadStats(profileId)
    }

    val isChildMode = !profileId.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isChildMode) "My Progress & Performance" else "Student Progress & Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = CharcoalTextPrimary
                        )
                    }
                },
                actions = {
                    // Export Button
                    IconButton(onClick = { showExportOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Export Report",
                            tint = TerracottaAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperCreamBackground)
            )
        },
        containerColor = PaperCreamBackground
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TerracottaAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = DesignTokens.Spacing.lg)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                // ==========================================
                // 1. STUDENT PROFILE SELECTOR TABS (Visible in Parent mode)
                // ==========================================
                if (!isChildMode && uiState.allProfiles.size > 1) {
                    val selectedIdx = uiState.allProfiles.indexOfFirst { it.id == uiState.activeProfileId }.coerceAtLeast(0)

                    ScrollableTabRow(
                        selectedTabIndex = selectedIdx,
                        containerColor = PaperCreamSurface,
                        contentColor = TerracottaAccent,
                        edgePadding = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(12.dp))
                    ) {
                        uiState.allProfiles.forEachIndexed { index, profile ->
                            Tab(
                                selected = selectedIdx == index,
                                onClick = { viewModel.selectProfile(profile.id) },
                                text = {
                                    Text(
                                        text = profile.displayName,
                                        fontWeight = if (selectedIdx == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedIdx == index) TerracottaAccent else CharcoalTextSecondary
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
                }

                val stats = uiState.stats
                if (stats == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active student profile selected.", color = CharcoalTextSecondary)
                    }
                } else {
                    // Student Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stats.profile.displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            Text(
                                text = "${stats.profile.gradeLevel} • Reading with ${stats.profile.preferredFontId.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalTextSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = { showExportOptions = true },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerracottaAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", color = TerracottaAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                    // ==========================================
                    // 2. HIGHLIGHTS STATS GRID (4 Uncluttered Cards)
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
                    ) {
                        // Pages This Week
                        StatSummaryCard(
                            title = "Pages Read",
                            value = "${stats.weeklyPagesRead}",
                            subtext = "${stats.totalPagesRead} total pages",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            accentColor = CalmTeal,
                            modifier = Modifier.weight(1f)
                        )

                        // Reading Time This Week
                        StatSummaryCard(
                            title = "Reading Time",
                            value = "${stats.weeklyReadingMinutes}m",
                            subtext = "${stats.totalReadingMinutes}m overall",
                            icon = Icons.Default.Schedule,
                            accentColor = TerracottaAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
                    ) {
                        // Mastered Words
                        StatSummaryCard(
                            title = "Mastered Words",
                            value = "${stats.masteredWordsCount}",
                            subtext = "positive vocabulary bank",
                            icon = Icons.Default.Star,
                            accentColor = WarmForestGreen,
                            modifier = Modifier.weight(1f)
                        )

                        // Active Days
                        StatSummaryCard(
                            title = "Active Days",
                            value = "${stats.activeDaysThisWeek}/7",
                            subtext = "calm weekly streak",
                            icon = Icons.Default.Spa,
                            accentColor = CalmTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

                    // ==========================================
                    // 3. UNCLUTTERED WEEKLY READING TIME BAR CHART
                    // ==========================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                    ) {
                        Column(modifier = Modifier.padding(DesignTokens.Spacing.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Weekly Reading Minutes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "Individual daily reading time (No peer comparisons)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }
                                Icon(Icons.Default.Spa, contentDescription = null, tint = WarmForestGreen)
                            }

                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                            // Bar Chart Display
                            val maxMinutes = (stats.dailyMinutesThisWeek.maxOfOrNull { it.minutesRead } ?: 20).coerceAtLeast(20)

                            Row(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                stats.dailyMinutesThisWeek.forEach { dayStat ->
                                    val barHeightFraction = (dayStat.minutesRead.toFloat() / maxMinutes).coerceIn(0.05f, 1f)
                                    val isActive = dayStat.minutesRead > 0

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isActive) {
                                            Text(
                                                text = "${dayStat.minutesRead}m",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarmForestGreen
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height((80 * barHeightFraction).dp)
                                                .background(
                                                    color = if (isActive) WarmForestGreen else PaperCreamBorder,
                                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = dayStat.dayOfWeek,
                                            fontSize = 11.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive) CharcoalTextPrimary else CharcoalTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                    // ==========================================
                    // 4. MASTERED WORDS PREVIEW CHIPS
                    // ==========================================
                    if (stats.masteredWords.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                        ) {
                            Column(modifier = Modifier.padding(DesignTokens.Spacing.lg)) {
                                Text(
                                    text = "Mastered Vocabulary (${stats.masteredWords.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                                Text(
                                    text = "Words pronounced with confidence and accuracy:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    stats.masteredWords.take(15).forEach { wordItem ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WarmForestGreenContainer,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmForestGreen.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = WarmForestGreen, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = wordItem.word,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarmForestGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))
                    }

                    // ==========================================
                    // 5. TEACHER & PARENT NOTES SECTION
                    // ==========================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                    ) {
                        Column(modifier = Modifier.padding(DesignTokens.Spacing.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Teacher & Parent Notes (${stats.teacherNotes.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "Qualitative observations and encouragement",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }

                                Button(
                                    onClick = viewModel::openAddNoteDialog,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Note", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                            if (stats.teacherNotes.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PaperCreamBackground,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = DesignTokens.Spacing.sm)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(DesignTokens.Spacing.lg),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = CharcoalTextSecondary, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                                        Text(
                                            text = "No notes recorded yet. Add phonetic notes or encouragement for this student!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CharcoalTextSecondary
                                        )
                                    }
                                }
                            } else {
                                stats.teacherNotes.forEach { note ->
                                    TeacherNoteItem(
                                        note = note,
                                        onDelete = { viewModel.deleteTeacherNote(note.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
                }
            }
        }
    }

    // ==========================================
    // 6. ADD TEACHER NOTE DIALOG
    // ==========================================
    if (uiState.isAddNoteDialogOpen) {
        var noteText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Observation") }
        var authorName by remember { mutableStateOf("Teacher") }

        val categories = listOf("Observation", "Phonics", "Praise", "Pacing")

        AlertDialog(
            onDismissRequest = viewModel::closeAddNoteDialog,
            title = {
                Text(text = "Add Student Note", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
            },
            text = {
                Column {
                    Text(text = "Category:", style = MaterialTheme.typography.labelMedium, color = CharcoalTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCategory == cat) TerracottaAccent else PaperCreamBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCategory == cat) TerracottaAccent else PaperCreamBorder),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategory == cat) Color.White else CharcoalTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Observation Note") },
                        placeholder = { Text("e.g. Recognizing conjuncts well, practicing 'sh' sounds.") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveTeacherNote(noteText, selectedCategory, authorName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddNoteDialog) {
                    Text("Cancel", color = CharcoalTextSecondary)
                }
            },
            containerColor = PaperCreamSurface
        )
    }

    // ==========================================
    // 7. EXPORT OPTIONS BOTTOM SHEET / DIALOG
    // ==========================================
    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = {
                Text(
                    text = "Export Progress Report",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose an accessible export format. All data is generated 100% locally on-device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                    // PDF Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PaperCreamBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PaperCreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportOptions = false
                                viewModel.exportAsPdf { file ->
                                    sharePdfFile(context, file)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(DesignTokens.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                            Column {
                                Text("Dyslexia-Friendly PDF", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                                Text("Warm paper palette, visual charts, and embedded typography.", fontSize = 11.sp, color = CharcoalTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    // Plain Text Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PaperCreamBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PaperCreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportOptions = false
                                viewModel.exportAsText { text ->
                                    sharePlainText(context, text)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(DesignTokens.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                            Column {
                                Text("Accessible Plain Text (.txt)", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                                Text("Clean formatted ASCII summary for easy sharing & printing.", fontSize = 11.sp, color = CharcoalTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportOptions = false }) {
                    Text("Close", color = CharcoalTextSecondary)
                }
            },
            containerColor = PaperCreamSurface
        )
    }
}

@Composable
fun StatSummaryCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = CharcoalTextSecondary, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
            Text(text = subtext, fontSize = 10.sp, color = CharcoalTextSecondary)
        }
    }
}

@Composable
fun TeacherNoteItem(
    note: TeacherNoteEntity,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(note.createdAt))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PaperCreamBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaperCreamBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TerracottaAccentContainer
                    ) {
                        Text(
                            text = note.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(text = "by ${note.authorName} • $dateStr", fontSize = 11.sp, color = CharcoalTextSecondary)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = CharcoalTextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${note.noteText}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalTextPrimary
            )
        }
    }
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Akshar DyslexiaReader Progress Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Student Report"))
    } catch (e: Exception) {
        // Fallback simple share
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Akshar Progress Report saved to ${file.name}")
        }
        context.startActivity(Intent.createChooser(intent, "Share Student Report"))
    }
}

private fun sharePlainText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Akshar DyslexiaReader Student Progress")
    }
    context.startActivity(Intent.createChooser(intent, "Share Student Progress"))
}
