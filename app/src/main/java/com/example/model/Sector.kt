package com.example.model

enum class Sector(
    val id: String,
    val title: String,
    val description: String,
    val startingCost: Double,
    val baseDailyGross: Double,
    val baseMaterialCost: Double,
    val baseRent: Double,
    val baseWages: Double,
    val baseStaff: Int,
    val prestigeScore: Int
) {
    MARKET(
        id = "market",
        title = "Süpermarket & Perakende",
        description = "Gıda ve temel tüketim ürünleri satışı. Hızlı nakit akışı ve dengeli kâr marjı.",
        startingCost = 50_000.0,
        baseDailyGross = 9_000.0,
        baseMaterialCost = 3_800.0,
        baseRent = 700.0,
        baseWages = 1_500.0,
        baseStaff = 3,
        prestigeScore = 10
    ),
    RESTAURANT(
        id = "restaurant",
        title = "Restoran & Kafe Zinciri",
        description = "Lezzetli menüler ve şık mekanlar. Yüksek kâr marjı, müşteri memnuniyeti odaklı.",
        startingCost = 180_000.0,
        baseDailyGross = 24_000.0,
        baseMaterialCost = 9_500.0,
        baseRent = 2_800.0,
        baseWages = 4_500.0,
        baseStaff = 7,
        prestigeScore = 25
    ),
    LOGISTICS(
        id = "logistics",
        title = "Lojistik & Kargo Taşımacılığı",
        description = "TIR filosu, dağıtım ağı ve depolar. B2B taşımacılık ve e-ticaret lojistiği.",
        startingCost = 450_000.0,
        baseDailyGross = 58_000.0,
        baseMaterialCost = 21_000.0,
        baseRent = 5_000.0,
        baseWages = 11_000.0,
        baseStaff = 15,
        prestigeScore = 50
    ),
    MANUFACTURING(
        id = "manufacturing",
        title = "Sanayi & Üretim Fabrikası",
        description = "Endüstriyel mamul üretimi ve ihracat. Diğer şirketlere hammadde ve ürün satışı.",
        startingCost = 1_200_000.0,
        baseDailyGross = 145_000.0,
        baseMaterialCost = 56_000.0,
        baseRent = 12_000.0,
        baseWages = 28_000.0,
        baseStaff = 35,
        prestigeScore = 120
    ),
    CONSTRUCTION(
        id = "construction",
        title = "İnşaat & Gayrimenkul Geliştirme",
        description = "Lüks konut projeleri, iş merkezleri ve plazalar. Büyük proje satışları ve kira getirisi.",
        startingCost = 3_500_000.0,
        baseDailyGross = 410_000.0,
        baseMaterialCost = 160_000.0,
        baseRent = 25_000.0,
        baseWages = 75_000.0,
        baseStaff = 80,
        prestigeScore = 300
    ),
    TECHNOLOGY(
        id = "technology",
        title = "Teknoloji, Yazılım & Yapay Zeka",
        description = "SaaS bulut sistemleri, yapay zeka modelleri ve mikroçip Ar-Ge. Üssel değerleme artışı.",
        startingCost = 8_000_000.0,
        baseDailyGross = 980_000.0,
        baseMaterialCost = 180_000.0,
        baseRent = 45_000.0,
        baseWages = 220_000.0,
        baseStaff = 120,
        prestigeScore = 750
    ),
    TOURISM(
        id = "tourism",
        title = "Turizm, Otelcilik & Tatil Köyü",
        description = "Ege ve Akdeniz'de 5 yıldızlı tatil köyleri. Döviz girdisi ve yüksek yaz sezonu geliri.",
        startingCost = 15_000_000.0,
        baseDailyGross = 1_850_000.0,
        baseMaterialCost = 450_000.0,
        baseRent = 110_000.0,
        baseWages = 380_000.0,
        baseStaff = 250,
        prestigeScore = 1500
    ),
    FINANCE(
        id = "finance",
        title = "Yatırım Bankacılığı & Portföy",
        description = "Özel bankacılık, girişim sermayesi ve varlık yönetimi. Muazzam günlük sermaye getirisi.",
        startingCost = 50_000_000.0,
        baseDailyGross = 6_200_000.0,
        baseMaterialCost = 650_000.0,
        baseRent = 320_000.0,
        baseWages = 1_250_000.0,
        baseStaff = 400,
        prestigeScore = 4000
    );

    fun calculateDailyNetProfit(level: Int = 1): Double {
        val multiplier = 1.0 + (level - 1) * 0.85
        val gross = baseDailyGross * multiplier
        val costs = (baseMaterialCost + baseRent + baseWages) * multiplier
        val tax = (gross - costs).coerceAtLeast(0.0) * 0.20
        return gross - costs - tax
    }
}
