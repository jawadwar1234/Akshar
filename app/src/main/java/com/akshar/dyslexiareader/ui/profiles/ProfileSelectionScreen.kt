package com.akshar.dyslexiareader.ui.profiles

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@Composable
fun ProfileSelectionScreen(
    viewModel: ProfilesViewModel,
    onProfileSelected: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnlockSuccess) {
        if (uiState.isUnlockSuccess && uiState.selectedProfile != null) {
            onProfileSelected(uiState.selectedProfile!!.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCreamBackground)
            .padding(DesignTokens.Spacing.lg),
        horizontalAlignment = Alignment.Start
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TerracottaAccent
            )
            // Settings button with visible label
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.height(44.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        Text(
            text = stringResource(R.string.profiles_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
        Text(
            text = "Select your profile to start reading textbooks.",
            style = MaterialTheme.typography.bodyLarge,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

        // Profiles Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            contentPadding = PaddingValues(bottom = DesignTokens.Spacing.xxl),
            modifier = Modifier.weight(1f)
        ) {
            items(profiles) { profile ->
                ProfileCard(
                    profile = profile,
                    onClick = { viewModel.selectProfileToUnlock(profile) }
                )
            }

            // Add Profile Card
            item {
                AddProfileCard(
                    onClick = { viewModel.openAddProfileDialog() }
                )
            }
        }
    }

    // PIN Unlock Dialog / Overlay
    if (uiState.unlockingProfile != null) {
        PinUnlockScreen(
            profile = uiState.unlockingProfile!!,
            uiState = uiState,
            onNumericDigit = viewModel::onNumericPinDigit,
            onNumericBackspace = viewModel::onNumericPinBackspace,
            onPictureIconSelected = viewModel::onPicturePinIconSelected,
            onPictureClear = viewModel::clearPicturePin,
            onDismiss = viewModel::dismissUnlock
        )
    }

    // Add Profile Dialog
    if (uiState.isAddProfileDialogOpen) {
        AddProfileDialog(
            onDismiss = viewModel::closeAddProfileDialog,
            onConfirm = { name, grade, pinType, pin, avatar ->
                viewModel.createNewProfile(name, grade, pinType, pin, avatar)
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: ChildProfileEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(TerracottaAccent.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = profile.displayName,
                    tint = TerracottaAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Text(
                text = profile.gradeLevel,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalTextSecondary
            )
        }
    }
}

@Composable
fun AddProfileCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .border(
                width = DesignTokens.ElevationAndBorders.borderThin,
                color = CalmTeal.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(CalmTeal.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CalmTeal,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.profiles_add_student),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CalmTeal
            )
        }
    }
}
