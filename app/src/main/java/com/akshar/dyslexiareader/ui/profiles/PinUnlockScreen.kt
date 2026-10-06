package com.akshar.dyslexiareader.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.GentleErrorMuted
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.util.PinSecurityUtil

@Composable
fun PinUnlockScreen(
    profile: ChildProfileEntity,
    uiState: ProfilesUiState,
    onNumericDigit: (String) -> Unit,
    onNumericBackspace: () -> Unit,
    onPictureIconSelected: (String) -> Unit,
    onPictureClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPicturePin = profile.pinType.equals("PICTURE", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(DesignTokens.Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .border(
                        width = DesignTokens.ElevationAndBorders.borderThin,
                        color = PaperCreamBorder,
                        shape = MaterialTheme.shapes.large
                    ),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = PaperCreamBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_close),
                                tint = CharcoalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    Text(
                        text = if (isPicturePin) stringResource(R.string.pin_picture_prompt) else stringResource(R.string.pin_number_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    // Indicator Dots
                    if (isPicturePin) {
                        PicturePinIndicators(enteredCount = uiState.enteredPicturePinIcons.size)
                    } else {
                        NumericPinIndicators(enteredCount = uiState.enteredNumericPin.length)
                    }

                    if (uiState.pinErrorMessage != null) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (uiState.lockoutRemainingSeconds > 0) CalmTeal.copy(alpha = 0.12f) else GentleErrorMuted.copy(alpha = 0.10f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(DesignTokens.Spacing.sm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.pinErrorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.lockoutRemainingSeconds > 0) CalmTeal else GentleErrorMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                    // Keypad (Active only when not locked out)
                    if (uiState.lockoutRemainingSeconds <= 0) {
                        if (isPicturePin) {
                            PicturePinKeypad(
                                onIconSelected = onPictureIconSelected,
                                onClear = onPictureClear
                            )
                        } else {
                            NumericPinKeypad(
                                onDigit = onNumericDigit,
                                onBackspace = onNumericBackspace
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = CalmTeal,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                                Text(
                                    text = "Ready in ${uiState.lockoutRemainingSeconds}s",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CalmTeal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PicturePinIndicators(enteredCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isFilled = index < enteredCount
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = if (isFilled) TerracottaAccent else PaperCreamSurface,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = if (isFilled) TerracottaAccent else PaperCreamBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun NumericPinIndicators(enteredCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val isFilled = index < enteredCount
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = if (isFilled) CalmTeal else PaperCreamSurface,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = if (isFilled) CalmTeal else PaperCreamBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PicturePinKeypad(
    onIconSelected: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            modifier = Modifier.height(260.dp)
        ) {
            items(PinSecurityUtil.PICTURE_PIN_ICONS) { icon ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PaperCreamSurface, shape = MaterialTheme.shapes.medium)
                        .border(
                            width = DesignTokens.ElevationAndBorders.borderThin,
                            color = PaperCreamBorder,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onIconSelected(icon.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon.symbol,
                        fontSize = 32.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.height(44.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = stringResource(R.string.action_clear), color = CharcoalTextSecondary)
        }
    }
}

@Composable
private fun NumericPinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "back")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Spacer(modifier = Modifier.size(68.dp))
                        }
                        "back" -> {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(PaperCreamSurface, shape = CircleShape)
                                    .border(1.dp, PaperCreamBorder, CircleShape)
                                    .clickable { onBackspace() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = CharcoalTextPrimary
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(PaperCreamSurface, shape = CircleShape)
                                    .border(1.dp, PaperCreamBorder, CircleShape)
                                    .clickable { onDigit(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
