package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.*
import com.example.ui.components.PriceSparkline
import com.example.ui.components.StockTerminalModal
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun MarketsScreen(
    state: HoldingGameState,
    onBuyStock: (String, Int) -> Unit,
    onSellStock: (String, Int) -> Unit,
    onBuyCurrency: (String, Double) -> Unit,
    onSellCurrency: (String, Double) -> Unit,
    onBuyProperty: (String) -> Unit,
    onSellAllStock: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mainMarketTab by remember { mutableStateOf(0) } // 0: BIST Hisseleri, 1: Döviz & Altın, 2: Gayrimenkul
    var selectedSectorFilter by remember { mutableStateOf<Sector?>(null) }
    var showOnlyOwned by remember { mutableStateOf(false) }

    var selectedStockForTerminal by remember { mutableStateOf<BistStock?>(null) }
    var selectedCurrencyForTrade by remember { mutableStateOf<CurrencyAsset?>(null) }

    // If terminal is open, keep stock state live
    val liveActiveStock = selectedStockForTerminal?.let { cur ->
        state.stocks.find { it.ticker == cur.ticker }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Market Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkNavyCard)
                .padding(4.dp)
        ) {
            MarketSubTabItem(
                title = "📈 Borsa & Sektörler",
                selected = mainMarketTab == 0,
                onClick = { mainMarketTab = 0 },
                modifier = Modifier.weight(1.2f)
            )
            MarketSubTabItem(
                title = "💵 Döviz & Altın",
                selected = mainMarketTab == 1,
                onClick = { mainMarketTab = 1 },
                modifier = Modifier.weight(1f)
            )
            MarketSubTabItem(
                title = "🏰 Gayrimenkul",
                selected = mainMarketTab == 2,
                onClick = { mainMarketTab = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        when (mainMarketTab) {
            0 -> RealTimeBistStocksScreen(
                stocks = state.stocks,
                cash = state.cash,
                economy = state.economy,
                selectedSector = selectedSectorFilter,
                showOnlyOwned = showOnlyOwned,
                onSelectSector = {
                    showOnlyOwned = false
                    selectedSectorFilter = it
                },
                onToggleOwnedOnly = {
                    showOnlyOwned = !showOnlyOwned
                    if (showOnlyOwned) selectedSectorFilter = null
                },
                onOpenTerminal = { selectedStockForTerminal = it }
            )
            1 -> CurrenciesTab(
                currencies = state.currencies,
                cash = state.cash,
                onTradeClick = { selectedCurrencyForTrade = it }
            )
            2 -> RealEstateTab(
                properties = state.properties,
                cash = state.cash,
                onBuy = onBuyProperty
            )
        }
    }

    // Interactive Real-Time Stock Terminal Modal
    liveActiveStock?.let { stock ->
        StockTerminalModal(
            stock = stock,
            cash = state.cash,
            onDismiss = { selectedStockForTerminal = null },
            onBuy = { shares -> onBuyStock(stock.ticker, shares) },
            onSell = { shares -> onSellStock(stock.ticker, shares) },
            onSellAll = { onSellAllStock(stock.ticker) }
        )
    }

    // Currency Trade Modal
    selectedCurrencyForTrade?.let { cur ->
        CurrencyTradeModal(
            currency = cur,
            cash = state.cash,
            onDismiss = { selectedCurrencyForTrade = null },
            onBuy = { amount ->
                onBuyCurrency(cur.code, amount)
                selectedCurrencyForTrade = null
            },
            onSell = { amount ->
                onSellCurrency(cur.code, amount)
                selectedCurrencyForTrade = null
            }
        )
    }
}

@Composable
private fun RealTimeBistStocksScreen(
    stocks: List<BistStock>,
    cash: Double,
    economy: EconomyIndicators,
    selectedSector: Sector?,
    showOnlyOwned: Boolean,
    onSelectSector: (Sector?) -> Unit,
    onToggleOwnedOnly: () -> Unit,
    onOpenTerminal: (BistStock) -> Unit
) {
    val totalStockValuation = remember(stocks) { stocks.sumOf { it.totalValue } }
    val totalStockProfitLoss = remember(stocks) { stocks.sumOf { it.totalProfitLoss } }

    val filteredStocks = remember(stocks, selectedSector, showOnlyOwned) {
        stocks.filter { stock ->
            if (showOnlyOwned) {
                stock.ownedShares > 0
            } else if (selectedSector != null) {
                stock.sector == selectedSector
            } else {
                true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Live Borsa Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "BIST 100 CANLI PİYASA",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = EmeraldLight
                        )
                    }

                    Text(
                        text = "Endeks: ${Formatters.formatRate(economy.bist100Index)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Portfolio Summary Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavySurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hisse Portföy Değeri", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                        Text(
                            Formatters.formatLira(totalStockValuation),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Toplam Hisse K/Z", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                        val isPos = totalStockProfitLoss >= 0
                        Text(
                            text = "${if (isPos) "+" else ""}${Formatters.formatLira(totalStockProfitLoss)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPos) EmeraldProfit else RoseLoss
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Sektör Filtreleme Çubuğu (Horizontal Scrollable Filter Chips)
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipItem(
                label = "Tümü (${stocks.size})",
                selected = selectedSector == null && !showOnlyOwned,
                onClick = { onSelectSector(null) }
            )

            FilterChipItem(
                label = "⭐ Portföyüm (${stocks.count { it.ownedShares > 0 }})",
                selected = showOnlyOwned,
                onClick = onToggleOwnedOnly,
                activeColor = GoldPrimary
            )

            Sector.values().forEach { sector ->
                val count = stocks.count { it.sector == sector }
                val icon = when (sector) {
                    Sector.MARKET -> "🛒"
                    Sector.RESTAURANT -> "🍽️"
                    Sector.LOGISTICS -> "🚚"
                    Sector.MANUFACTURING -> "🏭"
                    Sector.CONSTRUCTION -> "🏗️"
                    Sector.TECHNOLOGY -> "💻"
                    Sector.TOURISM -> "🏖️"
                    Sector.FINANCE -> "🏦"
                }
                FilterChipItem(
                    label = "$icon ${sector.title.split(" ").first()} ($count)",
                    selected = selectedSector == sector && !showOnlyOwned,
                    onClick = { onSelectSector(sector) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Real-Time Stocks List
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 600.dp

            if (isWide) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredStocks, key = { it.ticker }) { stock ->
                        RealTimeStockCard(
                            stock = stock,
                            onOpenTerminal = { onOpenTerminal(stock) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredStocks, key = { it.ticker }) { stock ->
                        RealTimeStockCard(
                            stock = stock,
                            onOpenTerminal = { onOpenTerminal(stock) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeStockCard(
    stock: BistStock,
    onOpenTerminal: () -> Unit
) {
    // Pulse highlight animation on tick update
    val pulseBorderColor by animateColorAsState(
        targetValue = when (stock.lastTickDirection) {
            1 -> EmeraldProfit.copy(alpha = 0.8f)
            -1 -> RoseLoss.copy(alpha = 0.8f)
            else -> SlateBorder
        },
        label = "stock_card_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTerminal)
            .testTag("stock_card_${stock.ticker}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pulseBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Ticker, Name, Sector Pill, Sparkline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkNavySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                        ) {
                            Text(
                                text = stock.ticker,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = stock.sectorName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextLight,
                        maxLines = 1
                    )
                }

                // Mini Canvas sparkline of recent real-time ticks
                PriceSparkline(
                    points = if (stock.liveTicks.size >= 2) stock.liveTicks else stock.priceHistory,
                    width = 68.dp,
                    height = 28.dp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price Row & Day Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Formatters.formatLiraDetailed(stock.currentPrice),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
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
                                    fontSize = 10.sp
                                ),
                                color = if (isPos) EmeraldProfit else RoseLoss,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "D: ₺${Formatters.formatRate(stock.dayLow)} • Y: ₺${Formatters.formatRate(stock.dayHigh)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                }

                // Position or Trade Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stock.ownedShares > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${stock.ownedShares} Lot",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldLight
                            )
                            val isProfit = stock.totalProfitLoss >= 0
                            Text(
                                text = "${if (isProfit) "+" else ""}${Formatters.formatLira(stock.totalProfitLoss)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isProfit) EmeraldProfit else RoseLoss
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = onOpenTerminal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terminal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = ElectricBlue
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) activeColor else DarkNavyCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) activeColor else SlateBorder.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) DarkNavyBase else TextLight,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MarketSubTabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) GoldPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) DarkNavyBase else TextLight
        )
    }
}

// --- SUB-TAB 1: CURRENCIES & GOLD ---
@Composable
private fun CurrenciesTab(
    currencies: List<CurrencyAsset>,
    cash: Double,
    onTradeClick: (CurrencyAsset) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        items(currencies, key = { it.code }) { cur ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTradeClick(cur) },
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkNavyCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cur.symbol,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = cur.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Text(
                                    text = cur.code,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        PriceSparkline(points = cur.history, width = 74.dp, height = 30.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "₺${Formatters.formatRate(cur.currentRate)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            val isPos = cur.dailyChangePercent >= 0
                            Text(
                                text = Formatters.formatPercent(cur.dailyChangePercent),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isPos) EmeraldProfit else RoseLoss
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cur.ownedAmount > 0.01) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${Formatters.formatRate(cur.ownedAmount)} ${cur.code}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldLight
                                    )
                                    Text(
                                        text = Formatters.formatLira(cur.totalValueTry),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Button(
                                onClick = { onTradeClick(cur) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Al / Sat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 2: REAL ESTATE ---
@Composable
private fun RealEstateTab(
    properties: List<RealEstateProperty>,
    cash: Double,
    onBuy: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HolidayVillage,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Düzenli Pasif Kira Portföyü",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Gayrimenkuller her gün kasaya garantili kira geliri sağlar ve holding net değerini büyütür.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }
            }
        }

        items(properties, key = { it.id }) { prop ->
            val canAfford = cash >= prop.purchasePrice

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = prop.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = prop.location,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }

                        if (prop.ownedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldProfit.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldProfit.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${prop.ownedCount} Adet Sahip",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = EmeraldProfit,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavySurface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Satış Fiyatı", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                            Text(
                                Formatters.formatLiraCompact(prop.purchasePrice),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Günlük Kira", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                            Text(
                                "+${Formatters.formatLira(prop.dailyRentIncome)} / gün",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldProfit)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onBuy(prop.id) },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkNavyBase,
                            disabledContainerColor = DarkNavyCardElevated,
                            disabledContentColor = TextSubtle
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Text(
                            text = if (canAfford) "Mülkü Satın Al (${Formatters.formatLiraCompact(prop.purchasePrice)})" else "Yetersiz Nakit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyTradeModal(
    currency: CurrencyAsset,
    cash: Double,
    onDismiss: () -> Unit,
    onBuy: (Double) -> Unit,
    onSell: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("100") }
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val totalCost = currency.currentRate * amount
    val canBuy = amount > 0 && cash >= totalCost
    val canSell = amount > 0 && currency.ownedAmount >= amount

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        title = {
            Text("${currency.name} (${currency.code})", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Güncel Kur: ₺${Formatters.formatRate(currency.currentRate)}", color = GoldLight)
                Text("Mevcut Bakiye: ${Formatters.formatRate(currency.ownedAmount)} ${currency.code}", color = TextMuted)

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Miktar (${currency.code})") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Toplam TL Değeri: ${Formatters.formatLira(totalCost)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currency.ownedAmount > 0.01) {
                    Button(
                        onClick = { onSell(amount) },
                        enabled = canSell,
                        colors = ButtonDefaults.buttonColors(containerColor = RoseLoss)
                    ) {
                        Text("Bozdur (TL)", fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = { onBuy(amount) },
                    enabled = canBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit)
                ) {
                    Text("Döviz Al", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextMuted)
            }
        }
    )
}
