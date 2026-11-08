package com.example.model

data class ActiveLoan(
    val id: String,
    val name: String,
    val principalAmount: Double,
    val remainingBalance: Double,
    val dailyPayment: Double,
    val daysRemaining: Int,
    val interestRateAnnual: Double
)

data class BankData(
    val depositBalance: Double = 0.0,
    val activeLoans: List<ActiveLoan> = emptyList(),
    val creditScore: Int = 1150 // Out of 1900
) {
    val totalDebt: Double
        get() = activeLoans.sumOf { it.remainingBalance }

    val totalDailyLoanPayments: Double
        get() = activeLoans.sumOf { it.dailyPayment }

    fun calculateDailyDepositInterest(policyRate: Double): Double {
        if (depositBalance <= 0) return 0.0
        // Daily yield from annual rate (e.g. 45% / 365)
        val dailyRate = (policyRate * 0.95 / 100.0) / 365.0
        return depositBalance * dailyRate
    }
}

enum class MarketSentiment(val title: String, val colorHex: Long) {
    BULLISH("Yükseliş Trendi 🚀", 0xFF10B981),
    STABLE("Dengeli Piyasa ⚖️", 0xFF3B82F6),
    VOLATILE("Dalgalı / Belirsiz ⚡", 0xFFF59E0B),
    BEARISH("Düşüş / Kriz ⚠️", 0xFFEF4444)
}

data class EconomyIndicators(
    val day: Int = 1,
    val usdTry: Double = 34.50,
    val eurTry: Double = 37.80,
    val goldGramTry: Double = 3020.0,
    val inflationAnnual: Double = 44.5,
    val policyRate: Double = 48.0,
    val bist100Index: Double = 9850.0,
    val sentiment: MarketSentiment = MarketSentiment.STABLE
) {
    val formattedDate: String
        get() {
            // Day 1 corresponds to 15 Mart 2026
            val months = listOf(
                "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
            )
            val baseDay = 15
            val baseMonth = 2 // March is 0-indexed 2
            val baseYear = 2026

            val totalDays = baseDay + (day - 1)
            val monthOffset = (totalDays - 1) / 30
            val dayOfMonth = ((totalDays - 1) % 30) + 1
            val currentMonthIndex = (baseMonth + monthOffset) % 12
            val year = baseYear + (baseMonth + monthOffset) / 12

            return "$dayOfMonth ${months[currentMonthIndex]} $year"
        }
}

data class NewsEvent(
    val id: String,
    val day: Int,
    val title: String,
    val content: String,
    val category: String, // "TCMB", "Kriz", "Fırsat", "Borsa", "Sektör"
    val affectedSector: Sector? = null,
    val impactMultiplier: Double = 1.0,
    val durationDays: Int = 15
)

data class CompetitorHolding(
    val id: String,
    val name: String,
    val founder: String,
    val netWorth: Double,
    val primarySector: String,
    val isPlayer: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardTry: Double,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f,
    val targetLabel: String = ""
)
