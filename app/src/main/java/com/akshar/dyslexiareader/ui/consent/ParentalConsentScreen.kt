package com.akshar.dyslexiareader.ui.consent

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@Composable
fun ParentalConsentScreen(
    onConsentAccepted: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCreamBackground)
            .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // App Branding Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = PaperCreamSurface,
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = DesignTokens.ElevationAndBorders.borderThin,
                    color = PaperCreamBorder,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = TerracottaAccent,
                modifier = Modifier.size(DesignTokens.iconSizeSmall)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Title & Subtitle
        Text(
            text = stringResource(R.string.consent_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
        Text(
            text = stringResource(R.string.consent_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // DPDP Act 2023 Privacy Card
        Card(
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TerracottaAccent,
                        modifier = Modifier.size(DesignTokens.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                    Text(
                        text = stringResource(R.string.consent_privacy_heading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                ConsentBullet(text = stringResource(R.string.consent_bullet_1))
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                ConsentBullet(text = stringResource(R.string.consent_bullet_2))
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                ConsentBullet(text = stringResource(R.string.consent_bullet_3))
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                ConsentBullet(text = stringResource(R.string.consent_bullet_4))
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Checkbox with large 56dp+ touch target
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isChecked = !isChecked }
                .padding(vertical = DesignTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = TerracottaAccent,
                    uncheckedColor = CharcoalTextSecondary
                )
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
            Text(
                text = stringResource(R.string.consent_checkbox),
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Action Button
        Button(
            onClick = onConsentAccepted,
            enabled = isChecked,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaAccent,
                contentColor = Color.White,
                disabledContainerColor = PaperCreamBorder,
                disabledContentColor = CharcoalTextSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.consent_button_accept),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
    }
}

@Composable
private fun ConsentBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(TerracottaAccent, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextPrimary,
            lineHeight = 22.sp
        )
    }
}
