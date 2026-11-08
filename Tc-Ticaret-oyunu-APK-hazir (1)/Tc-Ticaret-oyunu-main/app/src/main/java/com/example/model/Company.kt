package com.example.model

enum class PricingStrategy(val title: String, val salesMultiplier: Double, val marginMultiplier: Double) {
    DISCOUNT("İndirimli (Sürüm)", 1.35, 0.85),
    BALANCED("Dengeli", 1.0, 1.0),
    PREMIUM("Lüks / Premium", 0.75, 1.38)
}

data class Company(
    val id: String,
    val name: String,
    val sector: Sector,
    val level: Int = 1,
    val staffCount: Int = sector.baseStaff,
    val pricingStrategy: PricingStrategy = PricingStrategy.BALANCED,
    val marketingDaysLeft: Int = 0,
    val automationLevel: Int = 0,
    val totalRevenueEarned: Double = 0.0,
    val foundedDay: Int = 1
) {
    val upgradeCost: Double
        get() = sector.startingCost * 0.75 * (level * 1.2)

    val automationUpgradeCost: Double
        get() = (sector.startingCost * 0.35) * (1.0 + automationLevel * 0.7)

    fun calculateDailyFinancials(
        cfoBonus: Boolean = false,
        cooBonus: Boolean = false,
        cmoBonus: Boolean = false,
        sectorEventMultiplier: Double = 1.0
    ): CompanyDailyFinancials {
        val levelMultiplier = 1.0 + (level - 1) * 0.85
        val marketingMultiplier = if (marketingDaysLeft > 0) 1.25 else 1.0
        val cmoMultiplier = if (cmoBonus) 1.15 else 1.0

        val grossRevenue = sector.baseDailyGross * levelMultiplier *
                pricingStrategy.salesMultiplier * pricingStrategy.marginMultiplier *
                marketingMultiplier * cmoMultiplier * sectorEventMultiplier

        val automationDiscount = (1.0 - (automationLevel * 0.08)).coerceAtLeast(0.60)
        val cooDiscount = if (cooBonus) 0.82 else 1.0

        val materialCost = sector.baseMaterialCost * levelMultiplier *
                pricingStrategy.salesMultiplier * automationDiscount * cooDiscount

        val rentCost = sector.baseRent * levelMultiplier * automationDiscount

        // Staff cost based on current staff count vs recommended staff
        val targetStaff = sector.baseStaff * level
        val staffRatio = (staffCount.toDouble() / targetStaff.coerceAtLeast(1)).coerceIn(0.5, 2.0)
        val effectiveGross = grossRevenue * staffRatio.coerceAtMost(1.15)

        val wagePerStaff = sector.baseWages / sector.baseStaff
        val totalWages = wagePerStaff * staffCount

        val preTaxProfit = (effectiveGross - materialCost - rentCost - totalWages)
        val taxRate = if (cfoBonus) 0.15 else 0.20
        val tax = if (preTaxProfit > 0) preTaxProfit * taxRate else 0.0
        val netProfit = preTaxProfit - tax

        return CompanyDailyFinancials(
            grossRevenue = effectiveGross,
            materialCost = materialCost,
            wageCost = totalWages,
            rentCost = rentCost,
            taxCost = tax,
            netProfit = netProfit
        )
    }

    val valuation: Double
        get() {
            val baseVal = sector.startingCost * (0.9 + level * 0.8) + (automationLevel * automationUpgradeCost * 0.6)
            val profitAnnualized = (sector.calculateDailyNetProfit(level) * 365.0).coerceAtLeast(0.0)
            return (baseVal * 0.6 + profitAnnualized * 0.4).coerceAtLeast(sector.startingCost * 0.8)
        }
}

data class CompanyDailyFinancials(
    val grossRevenue: Double,
    val materialCost: Double,
    val wageCost: Double,
    val rentCost: Double,
    val taxCost: Double,
    val netProfit: Double
)
