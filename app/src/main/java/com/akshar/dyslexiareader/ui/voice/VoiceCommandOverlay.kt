package com.akshar.dyslexiareader.ui.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import com.akshar.dyslexiareader.util.AppLanguage
import com.akshar.dyslexiareader.util.voice.VoiceCommand
import com.akshar.dyslexiareader.util.voice.VoiceCommandManager

@Composable
fun VoiceMicButton(
    voiceCommandManager: VoiceCommandManager,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceState by voiceCommandManager.state.collectAsState()
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceCommandManager.startListening(currentLanguage, handsFree = false)
        } else {
            showPermissionRationale = true
        }
    }

    // Pulsing animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Glowing animated aura when listening
        if (voiceState.isListening) {
            val dynamicScale = pulseScale + (voiceState.currentAmplitude * 0.3f)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(dynamicScale)
                    .background(TerracottaAccent.copy(alpha = 0.25f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(dynamicScale * 0.9f)
                    .background(TerracottaAccent.copy(alpha = 0.35f), shape = CircleShape)
            )
        }

        FloatingActionButton(
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    if (voiceState.isListening) {
                        voiceCommandManager.stopListening()
                    } else {
                        voiceCommandManager.startListening(currentLanguage, handsFree = voiceState.isHandsFreeMode)
                    }
                }
            },
            shape = CircleShape,
            containerColor = if (voiceState.isListening) TerracottaAccent else PaperCreamSurface,
            contentColor = if (voiceState.isListening) Color.White else TerracottaAccent,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (voiceState.isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                contentDescription = if (voiceState.isListening) "Listening for commands" else "Tap to speak command",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Permission Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            icon = { Icon(Icons.Default.Mic, contentDescription = null, tint = TerracottaAccent) },
            title = { Text("Microphone Permission", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary) },
            text = {
                Text(
                    "Akshar uses your microphone only to listen for reading voice commands like 'next line' or 'read to me'.\n\nYour voice stays 100% private on your device and is never recorded or sent anywhere.",
                    color = CharcoalTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                ) {
                    Text("Allow Microphone", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPermissionRationale = false }) {
                    Text("Not Now")
                }
            },
            containerColor = PaperCreamBackground
        )
    }

    // Missing Language Pack Helper Dialog
    if (voiceState.isLanguagePackMissing) {
        AlertDialog(
            onDismissRequest = { voiceCommandManager.dismissMissingLanguageDialog() },
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = CalmTeal) },
            title = { Text("Offline Voice Pack Needed", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary) },
            text = {
                Column {
                    Text(
                        "To use voice commands offline in ${currentLanguage.displayName}, you can download the free offline language pack in your Android settings:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                    Text("1. Tap 'Open Speech Settings' below", style = MaterialTheme.typography.bodySmall, color = CharcoalTextPrimary)
                    Text("2. Select 'Offline speech recognition'", style = MaterialTheme.typography.bodySmall, color = CharcoalTextPrimary)
                    Text("3. Choose '${currentLanguage.displayName}' and tap Download", style = MaterialTheme.typography.bodySmall, color = CharcoalTextPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        voiceCommandManager.dismissMissingLanguageDialog()
                        try {
                            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmTeal)
                ) {
                    Text("Open Speech Settings", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { voiceCommandManager.dismissMissingLanguageDialog() }) {
                    Text("Got It")
                }
            },
            containerColor = PaperCreamBackground
        )
    }
}

@Composable
fun VoiceListeningBanner(
    voiceCommandManager: VoiceCommandManager,
    onToggleHandsFree: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by voiceCommandManager.state.collectAsState()

    AnimatedVisibility(
        visible = voiceState.isListening || voiceState.feedbackMessage != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.xs)
                .border(DesignTokens.ElevationAndBorders.borderThin, TerracottaAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (voiceState.isListening) TerracottaAccent else CalmTeal,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                    Column {
                        Text(
                            text = if (voiceState.isListening) "Listening..." else "Voice Control",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaAccent
                        )
                        Text(
                            text = voiceState.feedbackMessage ?: "Say 'next line', 'read', 'stop', or 'bigger'",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextPrimary,
                            maxLines = 1
                        )
                    }
                }

                // Hands-Free Mode Toggle Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (voiceState.isHandsFreeMode) CalmTeal.copy(alpha = 0.15f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (voiceState.isHandsFreeMode) CalmTeal else PaperCreamBorder
                    ),
                    modifier = Modifier.clickable { onToggleHandsFree() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (voiceState.isHandsFreeMode) Icons.Default.VolumeUp else Icons.Default.TouchApp,
                            contentDescription = "Toggle Hands-Free Mode",
                            tint = if (voiceState.isHandsFreeMode) CalmTeal else CharcoalTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (voiceState.isHandsFreeMode) "Hands-Free On" else "Hands-Free",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voiceState.isHandsFreeMode) CalmTeal else CharcoalTextSecondary
                        )
                    }
                }
            }
        }
    }
}
