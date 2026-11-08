package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseLoss

@Composable
fun PriceSparkline(
    points: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = if (points.size >= 2 && points.last() >= points.first()) EmeraldProfit else RoseLoss,
    width: Dp = 80.dp,
    height: Dp = 32.dp
) {
    if (points.size < 2) return

    val min = points.minOrNull() ?: 0.0
    val max = points.maxOrNull() ?: 1.0
    val range = (max - min).coerceAtLeast(0.0001)

    Canvas(modifier = modifier.width(width).height(height)) {
        val w = size.width
        val h = size.height

        val stepX = w / (points.size - 1)
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, p ->
            val x = i * stepX
            val normalizedY = ((p - min) / range).toFloat()
            val y = h - (normalizedY * (h - 8f)) - 4f

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw current end dot
        val lastX = w
        val lastNormalizedY = ((points.last() - min) / range).toFloat()
        val lastY = h - (lastNormalizedY * (h - 8f)) - 4f
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}
