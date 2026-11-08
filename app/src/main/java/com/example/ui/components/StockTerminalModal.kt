package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BistStock
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun StockTerminalModal(
    stock: BistStock,
    cash: Double,
    onDismiss: () -> Unit,
    onBuy: (Int) -> Unit,
    onSell: (Int) -> Unit,
    onSellAll: () -> Unit
) {
    var isBuyTab by remember { mutableStateOf(true) }
    var sharesInput by remember { mutableStateOf("10") }
    var chartTimeframe by remember { mutableStateOf(0) } // 0: Canlı, 1: Günlük/Haftalık

    val shares = sharesInput.toIntOrNull() ?: 0
    val totalCost = stock.currentPrice * shares
    val canAfford = shares > 0 && cash >= totalCost
    val canSell = shares > 0 && stock.ownedShares >= shares

    val maxAffordableShares = (cash / stock.currentPrice).toInt().coerceAtLeast(0)

    val displayedPoints = when (chartTimeframe) {
        0 -> if (stock.liveTicks.size >= 2) stock.liveTicks else stock.priceHistory
        else -> stock.priceHistory
    }

    // Live tick pulse color animation
    val pulseColor by animateColorAsState(
        targetValue = when (stock.lastTickDirection) {
            1 -> EmeraldProfit.copy(alpha = 0.25f)
            -1 -> RoseLoss.copy(alpha = 0.25f)
            else -> DarkNavySurface
        },
        label = "tick_pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        modifier = Modifier.fillMaxWidth(0.96f),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stock.ticker,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = stock.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite,
                                maxLines = 1
                            )
                            Text(
                                text = stock.sectorName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = TextLight)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Live Indicator Ticker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(pulseColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldProfit)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatLiraDetailed(stock.currentPrice),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val isPos = stock.dailyChangePercent >= 0
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPos) EmeraldProfit.copy(alpha = 0.2f) else RoseLoss.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${if (isPos) "▲ +" else "▼ "}${Formatters.formatPercent(stock.dailyChangePercent, includeSign = false)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isPos) EmeraldProfit else RoseLoss,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Y: ₺${Formatters.formatRate(stock.dayHigh)} • D: ₺${Formatters.formatRate(stock.dayLow)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                        Text(
                            text = "Hacim: ${stock.volume / 1000}B Lot",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSubtle
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chart Timeframe toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeframeButton(
                        label = "⚡ Canlı Akış",
                        selected = chartTimeframe == 0,
                        onClick = { chartTimeframe = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeButton(
                        label = "📊 Seans Geçmişi",
                        selected = chartTimeframe == 1,
                        onClick = { chartTimeframe = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Interactive Chart
                InteractiveStockChart(
                    points = displayedPoints,
                    currentPrice = stock.currentPrice,
                    openPrice = stock.openPrice,
                    height = 140
                )

                // Live Order Book (Depth)
                OrderBookAndTape(currentPrice = stock.currentPrice)

                // Owned Position Card
                if (stock.ownedShares > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mevcut Portföy Pozisyonunuz",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldLight
                                )
                                TextButton(
                                    onClick = onSellAll,
                                    colors = ButtonDefaults.textButtonColors(contentColor = RoseLoss),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Hepsini Sat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Adet: ${stock.ownedShares}", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                                    Text("Ort. Maliyet: ₺${Formatters.formatRate(stock.avgPurchasePrice)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Piyasa Değeri: ${Formatters.formatLira(stock.totalValue)}", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                                    val isProfit = stock.totalProfitLoss >= 0
                                    Text(
                                        text = "${if (isProfit) "+" else ""}${Formatters.formatLira(stock.totalProfitLoss)} (${Formatters.formatPercent(stock.profitLossPercent)})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isProfit) EmeraldProfit else RoseLoss
                                    )
                                }
                            }
                        }
                    }
                }

                // Trading Order Form (Buy vs Sell)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Buy / Sell Tab selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkNavySurface)
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isBuyTab) EmeraldProfit else Color.Transparent)
                                    .clickable { isBuyTab = true }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ALIŞ YAP (BUY)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isBuyTab) DarkNavyBase else TextLight
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!isBuyTab) RoseLoss else Color.Transparent)
                                    .clickable { isBuyTab = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SATIŞ YAP (SELL)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (!isBuyTab) TextWhite else TextLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Shares Input
                        OutlinedTextField(
                            value = sharesInput,
                            onValueChange = { if (it.all { c -> c.isDigit() }) sharesInput = it },
                            label = { Text("İşlem Adedi (Lot)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isBuyTab) EmeraldProfit else RoseLoss,
                                unfocusedBorderColor = SlateBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stock_shares_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(10, 50, 100, 500).forEach { qty ->
                                OutlinedButton(
                                    onClick = { sharesInput = qty.toString() },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                ) {
                                    Text("+$qty", fontSize = 10.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    sharesInput = if (isBuyTab) {
                                        maxAffordableShares.toString()
                                    } else {
                                        stock.ownedShares.toString()
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                            ) {
                                Text("Maks", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Summary of Cost & Balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBuyTab) "Toplam Tutar:" else "Tahmini Gelir:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = Formatters.formatLira(totalCost),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isBuyTab) (if (canAfford) GoldLight else RoseLoss) else EmeraldLight
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBuyTab) "Kasadaki Nakit:" else "Portföydeki Adet:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                            Text(
                                text = if (isBuyTab) Formatters.formatLira(cash) else "${stock.ownedShares} Adet",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextLight
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Execute Button
                        if (isBuyTab) {
                            Button(
                                onClick = { onBuy(shares) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldProfit,
                                    contentColor = DarkNavyBase,
                                    disabledContainerColor = DarkNavySurface,
                                    disabledContentColor = TextSubtle
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("execute_buy_button")
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canAfford) "Alış Emri Ver (${Formatters.formatLira(totalCost)})" else "Yetersiz Nakit Bakiye",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = { onSell(shares) },
                                enabled = canSell,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoseLoss,
                                    contentColor = TextWhite,
                                    disabledContainerColor = DarkNavySurface,
                                    disabledContentColor = TextSubtle
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("execute_sell_button")
                            ) {
                                Icon(imageVector = Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canSell) "Satış Emri Ver (${Formatters.formatLira(totalCost)})" else "Yetersiz Hisse Miktarı",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TimeframeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) ElectricBlue else DarkNavySurface)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (selected) TextWhite else TextLight
        )
    }
}
