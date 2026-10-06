package com.akshar.dyslexiareader.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    if (state.isAuthenticated) {
        onAuthSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCreamBackground)
            .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(PaperCreamSurface, shape = MaterialTheme.shapes.medium)
                .border(
                    width = DesignTokens.ElevationAndBorders.borderThin,
                    color = PaperCreamBorder,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = CalmTeal,
                modifier = Modifier.size(DesignTokens.iconSizeSmall)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        Text(
            text = if (state.isRegisterMode) stringResource(R.string.auth_btn_register) else stringResource(R.string.auth_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
        Text(
            text = stringResource(R.string.auth_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

        // Email field
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.auth_email_label)) },
            leadingIcon = {
                Icon(Icons.Default.Mail, contentDescription = null, tint = CharcoalTextSecondary)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

        // Password field
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.auth_password_label)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = CharcoalTextSecondary)
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerracottaAccent,
                unfocusedBorderColor = PaperCreamBorder,
                focusedContainerColor = PaperCreamSurface,
                unfocusedContainerColor = PaperCreamSurface
            ),
            shape = MaterialTheme.shapes.medium
        )

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Text(
                text = state.errorMessage ?: "",
                color = GentleErrorMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Submit Button
        Button(
            onClick = viewModel::loginWithEmail,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (state.isRegisterMode) stringResource(R.string.auth_btn_register) else stringResource(R.string.auth_btn_login),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

        // Toggle register / login
        TextButton(
            onClick = viewModel::toggleMode,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (state.isRegisterMode) "Already have an account? Sign In" else "New teacher or parent? Create Account",
                style = MaterialTheme.typography.bodyMedium,
                color = CalmTeal
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Google Sign-in
        OutlinedButton(
            onClick = viewModel::loginWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = PaperCreamSurface,
                contentColor = CharcoalTextPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(PaperCreamBorder)
            )
        ) {
            Text(
                text = stringResource(R.string.auth_btn_google),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Continue in Offline Mode
        Button(
            onClick = viewModel::continueInOfflineMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = PaperCreamSurface,
                contentColor = CalmTeal
            ),
            border = androidx.compose.foundation.BorderStroke(
                DesignTokens.ElevationAndBorders.borderThin,
                PaperCreamBorder
            )
        ) {
            Icon(Icons.Default.OfflinePin, contentDescription = null, tint = CalmTeal)
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.auth_btn_offline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
        Text(
            text = stringResource(R.string.auth_guest_notice),
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalTextSecondary,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs)
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
    }
}
