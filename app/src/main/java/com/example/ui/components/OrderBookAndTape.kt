package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun OrderBookAndTape(
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    // Generate simulated depth around current price
    val step = (currentPrice * 0.0015).coerceAtLeast(0.05)
    val round2 = { v: Double -> Math.round(v * 100.0) / 100.0 }

    val bids = remember(currentPrice) {
        listOf(
            Triple(round2(currentPrice - step * 1), 3400, 0.85f),
            Triple(round2(currentPrice - step * 2), 6200, 0.60f),
            Triple(round2(currentPrice - step * 3), 11500, 0.40f)
        )
    }

    val asks = remember(currentPrice) {
        listOf(
            Triple(round2(currentPrice + step * 1), 2800, 0.70f),
            Triple(round2(currentPrice + step * 2), 5400, 0.50f),
            Triple(round2(currentPrice + step * 3), 9800, 0.35f)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavyCard)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Canlı Alış / Satış Derinlik Tablosu",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Text(
                text = "Fark (Spread): ₺${Formatters.formatRate(step * 2)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ALIŞ (BİD)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = EmeraldLight)
            Text("SATIŞ (ASK)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = RoseLight)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Rows for Bids & Asks side-by-side
        for (i in 0..2) {
            val bid = bids[i]
            val ask = asks[i]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bid Column with green depth bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkNavySurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(bid.third)
                            .align(Alignment.CenterEnd)
                            .background(EmeraldProfit.copy(alpha = 0.22f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₺${Formatters.formatRate(bid.first)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = EmeraldProfit
                        )
                        Text(
                            text = "${bid.second}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextLight
                        )
                    }
                }

                // Ask Column with red depth bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkNavySurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ask.third)
                            .align(Alignment.CenterStart)
                            .background(RoseLoss.copy(alpha = 0.22f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ask.second}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextLight
                        )
                        Text(
                            text = "₺${Formatters.formatRate(ask.first)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = RoseLoss
                        )
                    }
                }
            }
        }
    }
}
