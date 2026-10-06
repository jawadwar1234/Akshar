package com.akshar.dyslexiareader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.akshar.dyslexiareader.ui.theme.LocalReaderTheme
import kotlin.math.roundToInt

@Composable
fun ReadingRulerOverlay(
    modifier: Modifier = Modifier,
    rulerHeightDp: Int = 64
) {
    val theme = LocalReaderTheme.current
    val density = LocalDensity.current
    var rulerOffsetY by remember { mutableFloatStateOf(200f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rulerOffsetY = (rulerOffsetY + dragAmount.y).coerceIn(40f, size.height - 180f)
                }
            }
    ) {
        val topDimHeightDp = with(density) { rulerOffsetY.toDp() }
        val bottomOffsetPx = (rulerOffsetY + with(density) { rulerHeightDp.dp.toPx() }).roundToInt()

        // Dimmed area above ruler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topDimHeightDp)
                .background(Color.Black.copy(alpha = 0.28f))
        )

        // Clear Tinted Focus Ruler Strip
        Box(
            modifier = Modifier
                .offset { IntOffset(0, rulerOffsetY.roundToInt()) }
                .fillMaxWidth()
                .height(rulerHeightDp.dp)
                .background(theme.rulerColor)
        )

        // Dimmed area below ruler
        Box(
            modifier = Modifier
                .offset { IntOffset(0, bottomOffsetPx) }
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )
    }
}
