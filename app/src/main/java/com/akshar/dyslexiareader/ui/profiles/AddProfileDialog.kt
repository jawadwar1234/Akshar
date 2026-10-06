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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
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

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, pinType: String, pin: String, avatar: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 3") }
    var pinType by remember { mutableStateOf("PICTURE") } // "PICTURE" or "NUMBER"
    var rawPin by remember { mutableStateOf("dog_star_apple") }
    val scrollState = rememberScrollState()

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
                    text = stringResource(R.string.profiles_pin_type_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

                // Picture PIN radio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pinType = "PICTURE"; rawPin = "dog_star_apple" }
                ) {
                    RadioButton(
                        selected = pinType == "PICTURE",
                        onClick = { pinType = "PICTURE"; rawPin = "dog_star_apple" },
                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaAccent)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = stringResource(R.string.profiles_pin_type_picture),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
                }

                // Numeric PIN radio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pinType = "NUMBER"; rawPin = "1234" }
                ) {
                    RadioButton(
                        selected = pinType == "NUMBER",
                        onClick = { pinType = "NUMBER"; rawPin = "1234" },
                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaAccent)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = stringResource(R.string.profiles_pin_type_number),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
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
                            if (name.isNotBlank()) {
                                onConfirm(name, grade, pinType, rawPin, "avatar_1")
                            }
                        },
                        enabled = name.isNotBlank(),
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
