package com.akshar.dyslexiareader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordInspectionDialog(
    wordSpan: WordSpan,
    onSpeakWord: (String) -> Unit,
    onSpeakWordSlowly: (String) -> Unit,
    onPracticeWord: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Word Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextSecondary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                // Big Word Display
                Text(
                    text = wordSpan.word,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // Syllable / Phonics Tiles
                if (wordSpan.syllables.isNotEmpty()) {
                    Text(
                        text = "Sounds in this word:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm)
                    ) {
                        wordSpan.syllables.forEachIndexed { index, syllable ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (index % 2 == 0) CalmTeal.copy(alpha = 0.12f) else TerracottaAccent.copy(alpha = 0.12f),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (index % 2 == 0) CalmTeal.copy(alpha = 0.4f) else TerracottaAccent.copy(alpha = 0.4f),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = syllable,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index % 2 == 0) CalmTeal else TerracottaAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // Actions: Listen Normal, Slow Syllables, Practice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { onSpeakWord(wordSpan.cleanWord) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f).height(DesignTokens.buttonHeight)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = CharcoalTextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Listen", color = CharcoalTextPrimary, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onSpeakWordSlowly(wordSpan.cleanWord) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1.1f).height(DesignTokens.buttonHeight)
                    ) {
                        Icon(Icons.Default.Hearing, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Slow Sounds", color = CalmTeal, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                Button(
                    onClick = {
                        onDismiss()
                        onPracticeWord(wordSpan.cleanWord)
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                    modifier = Modifier.fillMaxWidth().height(DesignTokens.buttonHeight)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text("Practice Saying This Word", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
