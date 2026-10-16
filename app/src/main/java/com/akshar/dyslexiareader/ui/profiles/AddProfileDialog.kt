package com.akshar.dyslexiareader.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.util.PinSecurityUtil

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, pinType: String, pin: String, avatar: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 3") }
    var pinType by remember { mutableStateOf("NUMBER") } // "PICTURE" or "NUMBER"
    var numericPin by remember { mutableStateOf("") }
    var selectedPictureIcons by remember { mutableStateOf(listOf("dog", "star", "apple")) }
    val scrollState = rememberScrollState()

    val isPinValid = if (pinType == "NUMBER") {
        numericPin.length == 4 && numericPin.all { it.isDigit() }
    } else {
        selectedPictureIcons.size == 3
    }

    val isFormValid = name.isNotBlank() && isPinValid

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                    .padding(DesignTokens.Spacing.lg)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.profiles_add_student),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = CharcoalTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profiles_name_label)) },
                    placeholder = { Text("e.g. Rohan, Ananya") },
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

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // Grade Field
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text(stringResource(R.string.profiles_grade_label)) },
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

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                Text(
                    text = "Child Security & Login PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )
                Text(
                    text = "Provide this PIN/password to the child so they can log in independently.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextSecondary
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                // Numeric PIN radio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pinType = "NUMBER" }
                ) {
                    RadioButton(
                        selected = pinType == "NUMBER",
                        onClick = { pinType = "NUMBER" },
                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaAccent)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = stringResource(R.string.profiles_pin_type_number) + " (4 digits)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
                }

                // Picture PIN radio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pinType = "PICTURE" }
                ) {
                    RadioButton(
                        selected = pinType == "PICTURE",
                        onClick = { pinType = "PICTURE" },
                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaAccent)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = stringResource(R.string.profiles_pin_type_picture) + " (3 icons)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                if (pinType == "NUMBER") {
                    OutlinedTextField(
                        value = numericPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                numericPin = input
                            }
                        },
                        label = { Text("Set 4-Digit Child PIN") },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaAccent,
                            unfocusedBorderColor = PaperCreamBorder,
                            focusedContainerColor = PaperCreamSurface,
                            unfocusedContainerColor = PaperCreamSurface
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    if (numericPin.isNotEmpty() && numericPin.length < 4) {
                        Text(
                            text = "Please enter all 4 digits (${numericPin.length}/4)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalTextSecondary,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PaperCreamSurface, shape = MaterialTheme.shapes.medium)
                            .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, MaterialTheme.shapes.medium)
                            .padding(DesignTokens.Spacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Sequence (${selectedPictureIcons.size}/3):",
                            style = MaterialTheme.typography.labelMedium,
                            color = CharcoalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { idx ->
                                val iconId = selectedPictureIcons.getOrNull(idx)
                                val iconObj = PinSecurityUtil.PICTURE_PIN_ICONS.firstOrNull { it.id == iconId }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(if (iconObj != null) TerracottaAccent.copy(alpha = 0.15f) else Color.Transparent, shape = CircleShape)
                                        .border(1.dp, if (iconObj != null) TerracottaAccent else PaperCreamBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = iconObj?.symbol ?: "?", fontSize = 20.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap 3 icons below to set PIN:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(90.dp)
                        ) {
                            items(PinSecurityUtil.PICTURE_PIN_ICONS) { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PaperCreamBackground, shape = MaterialTheme.shapes.small)
                                        .border(1.dp, PaperCreamBorder, MaterialTheme.shapes.small)
                                        .clickable {
                                            if (selectedPictureIcons.size >= 3) {
                                                selectedPictureIcons = listOf(icon.id)
                                            } else {
                                                selectedPictureIcons = selectedPictureIcons + icon.id
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon.symbol, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.height(DesignTokens.buttonHeight)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val rawPin = if (pinType == "NUMBER") {
                                    numericPin
                                } else {
                                    PinSecurityUtil.encodePicturePin(selectedPictureIcons)
                                }
                                onConfirm(name, grade, pinType, rawPin, "avatar_1")
                            }
                        },
                        enabled = isFormValid,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                        modifier = Modifier.height(DesignTokens.buttonHeight)
                    ) {
                        Text(stringResource(R.string.action_save), color = Color.White)
                    }
                }
            }
        }
    }
}
