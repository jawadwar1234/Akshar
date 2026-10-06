package com.akshar.dyslexiareader.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.ui.auth.AuthViewModel
import com.akshar.dyslexiareader.ui.profiles.ProfilesViewModel
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.GentleErrorMuted
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.util.AppLanguage
import com.akshar.dyslexiareader.util.LanguageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDataScreen(
    authViewModel: AuthViewModel,
    profilesViewModel: ProfilesViewModel,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userSettings by authViewModel.userSettings.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = CharcoalTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperCreamBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PaperCreamBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DesignTokens.Spacing.lg)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // App Language Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = DesignTokens.ElevationAndBorders.borderThin,
                        color = PaperCreamBorder,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { showLanguageDialog = true },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CalmTeal)
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                        Column {
                            Text(
                                text = stringResource(R.string.settings_section_language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalTextPrimary
                            )
                            Text(
                                text = userSettings.appLanguage.nativeName,
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.action_edit),
                        style = MaterialTheme.typography.labelMedium,
                        color = TerracottaAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

            // DPDP Act 2023 & Data Rights Section
            Text(
                text = stringResource(R.string.settings_section_privacy),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            // Export Data Button
            Button(
                onClick = {
                    val activeId = userSettings.activeProfileId ?: "default"
                    profilesViewModel.exportData(activeId) { jsonContent ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Akshar Textbook Data Export")
                            putExtra(Intent.EXTRA_TEXT, jsonContent)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export My Reading Data"))
                        scope.launch {
                            snackbarHostState.showSnackbar("Data exported successfully!")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = PaperCreamSurface, contentColor = CharcoalTextPrimary),
                border = androidx.compose.foundation.BorderStroke(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = TerracottaAccent)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                Text(
                    text = stringResource(R.string.settings_export_data),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // Delete Account and Erase All Data
            Button(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaperCreamSurface,
                    contentColor = GentleErrorMuted
                ),
                border = androidx.compose.foundation.BorderStroke(DesignTokens.ElevationAndBorders.borderThin, GentleErrorMuted.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = GentleErrorMuted)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                Text(
                    text = stringResource(R.string.settings_delete_account),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            // App Version Badge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
            ) {
                Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OfflinePin, contentDescription = null, tint = CalmTeal)
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                        Text(
                            text = stringResource(R.string.settings_about),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_section_language)) },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authViewModel.setLanguage(lang)
                                    LanguageManager.setAppLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = DesignTokens.Spacing.sm)
                        ) {
                            RadioButton(
                                selected = userSettings.appLanguage == lang,
                                onClick = {
                                    authViewModel.setLanguage(lang)
                                    LanguageManager.setAppLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TerracottaAccent)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CharcoalTextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_done), color = TerracottaAccent)
                }
            },
            containerColor = PaperCreamBackground
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = GentleErrorMuted) },
            title = { Text(stringResource(R.string.settings_delete_confirm_title), color = CharcoalTextPrimary) },
            text = { Text(stringResource(R.string.settings_delete_confirm_msg), color = CharcoalTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        profilesViewModel.deleteAllDataAndAccount {
                            onAccountDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GentleErrorMuted)
                ) {
                    Text(stringResource(R.string.action_delete), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = PaperCreamBackground
        )
    }
}
