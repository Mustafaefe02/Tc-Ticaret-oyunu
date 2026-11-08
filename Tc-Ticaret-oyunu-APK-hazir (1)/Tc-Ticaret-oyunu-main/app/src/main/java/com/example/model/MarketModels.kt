package com.example.model

data class BistStock(
    val ticker: String,
    val name: String,
    val sectorName: String,
    val sector: Sector? = null,
    val currentPrice: Double,
    val openPrice: Double,
    val dayHigh: Double = currentPrice,
    val dayLow: Double = currentPrice,
    val volume: Long = 1_500_000L,
    val priceHistory: List<Double>,
    val liveTicks: List<Double> = listOf(currentPrice),
    val ownedShares: Int = 0,
    val avgPurchasePrice: Double = 0.0,
    val lastTickDirection: Int = 0 // +1 (up/green), -1 (down/red), 0 (neutral)
) {
    val dailyChangePercent: Double
        get() = if (openPrice > 0) ((currentPrice - openPrice) / openPrice) * 100.0 else 0.0

    val totalValue: Double
        get() = ownedShares * currentPrice

    val totalProfitLoss: Double
        get() = if (ownedShares > 0) (currentPrice - avgPurchasePrice) * ownedShares else 0.0

    val profitLossPercent: Double
        get() = if (avgPurchasePrice > 0) ((currentPrice - avgPurchasePrice) / avgPurchasePrice) * 100.0 else 0.0
}

data class OrderBookLevel(
    val price: Double,
    val quantity: Int,
    val depthRatio: Float // 0f..1f for visual bar length
)

data class LiveTrade(
    val timeStr: String,
    val price: Double,
    val quantity: Int,
    val isBuy: Boolean
)

data class CurrencyAsset(
    val code: String,
    val name: String,
    val symbol: String,
    val currentRate: Double,
    val openRate: Double,
    val history: List<Double>,
    val ownedAmount: Double = 0.0,
    val avgBuyRate: Double = 0.0
) {
    val dailyChangePercent: Double
        get() = if (openRate > 0) ((currentRate - openRate) / openRate) * 100.0 else 0.0

    val totalValueTry: Double
        get() = ownedAmount * currentRate

    val totalProfitLossTry: Double
        get() = if (ownedAmount > 0) (currentRate - avgBuyRate) * ownedAmount else 0.0
}

data class RealEstateProperty(
    val id: String,
    val name: String,
    val location: String,
    val purchasePrice: Double,
    val dailyRentIncome: Double,
    val ownedCount: Int = 0
) {
    val totalInvestment: Double
        get() = purchasePrice * ownedCount

    val totalDailyRent: Double
        get() = dailyRentIncome * ownedCount

    val annualizedReturnPercent: Double
        get() = ((dailyRentIncome * 365.0) / purchasePrice) * 100.0
}
