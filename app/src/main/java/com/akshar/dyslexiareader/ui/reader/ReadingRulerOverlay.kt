package com.akshar.dyslexiareader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.LocalReaderTheme
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import kotlin.math.roundToInt

@Composable
fun ReadingRulerOverlay(
    modifier: Modifier = Modifier,
    windowLines: Int = 1,
    onCloseRuler: (() -> Unit)? = null
) {
    val theme = LocalReaderTheme.current
    val density = LocalDensity.current
    val rulerHeightDp = if (windowLines >= 3) 140 else 56
    var rulerOffsetY by remember { mutableFloatStateOf(180f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    rulerOffsetY = (offset.y - (rulerHeightDp * density.density / 2))
                        .coerceIn(20f, size.height - 180f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rulerOffsetY = (rulerOffsetY + dragAmount.y).coerceIn(20f, size.height - 180f)
                }
            }
    ) {
        val topDimHeightDp = with(density) { rulerOffsetY.coerceAtLeast(0f).toDp() }
        val bottomOffsetPx = (rulerOffsetY + with(density) { rulerHeightDp.dp.toPx() }).roundToInt()

        // Dimmed area above ruler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topDimHeightDp)
                .background(Color.Black.copy(alpha = 0.32f))
        )

        // Clear Tinted Focus Ruler Strip
        Box(
            modifier = Modifier
                .offset { IntOffset(0, rulerOffsetY.roundToInt()) }
                .fillMaxWidth()
                .height(rulerHeightDp.dp)
                .background(theme.rulerColor),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Nudge Up / Down quick controls on the right side of the ruler
            Row(
                modifier = Modifier.padding(end = DesignTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { rulerOffsetY = (rulerOffsetY - 48f).coerceAtLeast(20f) },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = theme.surface.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move ruler up",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                FilledIconButton(
                    onClick = { rulerOffsetY = (rulerOffsetY + 48f).coerceIn(20f, 1800f) },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = theme.surface.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move ruler down",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Dimmed area below ruler
        Box(
            modifier = Modifier
                .offset { IntOffset(0, bottomOffsetPx) }
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
        )
    }
}
