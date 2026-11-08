package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun InteractiveStockChart(
    points: List<Double>,
    currentPrice: Double,
    openPrice: Double,
    modifier: Modifier = Modifier,
    height: Int = 180
) {
    if (points.size < 2) return

    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    val isPositive = (points.lastOrNull() ?: currentPrice) >= openPrice
    val chartColor = if (isPositive) EmeraldProfit else RoseLoss

    val min = points.minOrNull() ?: (currentPrice * 0.95)
    val max = points.maxOrNull() ?: (currentPrice * 1.05)
    val range = (max - min).coerceAtLeast(0.01)

    val activeIndex = scrubIndex?.coerceIn(0, points.size - 1) ?: (points.size - 1)
    val selectedPrice = points.getOrElse(activeIndex) { currentPrice }
    val priceDiff = selectedPrice - openPrice
    val pctDiff = if (openPrice > 0) (priceDiff / openPrice) * 100.0 else 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubbing Header display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Formatters.formatLiraDetailed(selectedPrice),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Text(
                    text = "${if (priceDiff >= 0) "+" else ""}${Formatters.formatLiraDetailed(priceDiff)} (${Formatters.formatPercent(pctDiff)})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (priceDiff >= 0) EmeraldProfit else RoseLoss
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = DarkNavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Text(
                    text = if (scrubIndex != null) "İmleç: ${activeIndex + 1}/${points.size}" else "Canlı Son Fiyat",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Interactive Canvas Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(DarkNavyCardElevated.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            val step = size.width / (points.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                            scrubIndex = idx
                        }
                    )
                }
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val step = size.width / (points.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                            scrubIndex = idx
                        },
                        onDragEnd = { scrubIndex = null },
                        onDragCancel = { scrubIndex = null },
                        onDrag = { change, _ ->
                            val step = size.width / (points.size - 1).coerceAtLeast(1)
                            val idx = (change.position.x / step).toInt().coerceIn(0, points.size - 1)
                            scrubIndex = idx
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val padTop = 16f
            val padBottom = 20f
            val usableHeight = h - padTop - padBottom

            // Draw horizontal reference lines (High, Open, Low)
            val openY = padTop + usableHeight - (((openPrice - min) / range).toFloat() * usableHeight).coerceIn(0f, usableHeight)
            drawLine(
                color = SlateBorder.copy(alpha = 0.7f),
                start = Offset(0f, openY),
                end = Offset(w, openY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            val stepX = w / (points.size - 1).coerceAtLeast(1)
            val linePath = Path()
            val fillPath = Path()

            points.forEachIndexed { i, p ->
                val x = i * stepX
                val normalizedY = ((p - min) / range).toFloat().coerceIn(0f, 1f)
                val y = padTop + usableHeight - (normalizedY * usableHeight)

                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(w, h)
            fillPath.close()

            // Area Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        chartColor.copy(alpha = 0.35f),
                        chartColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = padTop,
                    endY = h
                )
            )

            // Line Stroke
            drawPath(
                path = linePath,
                color = chartColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Draw Active / Selected scrub crosshair
            val scrubX = activeIndex * stepX
            val activeNormY = ((points[activeIndex] - min) / range).toFloat().coerceIn(0f, 1f)
            val scrubY = padTop + usableHeight - (activeNormY * usableHeight)

            if (scrubIndex != null) {
                // Vertical crosshair
                drawLine(
                    color = TextWhite.copy(alpha = 0.5f),
                    start = Offset(scrubX, 0f),
                    end = Offset(scrubX, h),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Glow dot at point
            drawCircle(
                color = chartColor.copy(alpha = 0.3f),
                radius = 7.dp.toPx(),
                center = Offset(scrubX, scrubY)
            )
            drawCircle(
                color = TextWhite,
                radius = 3.5.dp.toPx(),
                center = Offset(scrubX, scrubY)
            )
        }
    }
}
