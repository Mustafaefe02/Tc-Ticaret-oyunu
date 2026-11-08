package com.example.model

data class DailySummaryFinancials(
    val grossCompanyRevenue: Double,
    val companyOperatingCosts: Double,
    val companyWages: Double,
    val companyTaxes: Double,
    val executiveSalaries: Double,
    val bankDepositInterest: Double,
    val bankLoanPayments: Double,
    val realEstateRents: Double,
    val netDailyProfit: Double
)

data class HoldingGameState(
    val holdingName: String = "",
    val founderName: String = "Genç Girişimci",
    val isHoldingNameCustomized: Boolean = false,
    val cash: Double = 50_000.0,
    val companies: List<Company> = emptyList(),
    val stocks: List<BistStock> = defaultStocks(),
    val currencies: List<CurrencyAsset> = defaultCurrencies(),
    val properties: List<RealEstateProperty> = defaultProperties(),
    val executives: List<ExecutiveState> = defaultExecutives(),
    val bank: BankData = BankData(),
    val economy: EconomyIndicators = EconomyIndicators(),
    val newsHistory: List<NewsEvent> = defaultInitialNews(),
    val activeNewsEffects: List<NewsEvent> = emptyList(),
    val achievements: List<Achievement> = defaultAchievements()
) {
    val totalEmployees: Int
        get() = companies.sumOf { it.staffCount } + executives.count { it.isHired }

    val totalCompanyValuation: Double
        get() = companies.sumOf { it.valuation }

    val totalStockValuation: Double
        get() = stocks.sumOf { it.totalValue }

    val totalCurrencyValuation: Double
        get() = currencies.sumOf { it.totalValueTry }

    val totalRealEstateValuation: Double
        get() = properties.sumOf { it.totalInvestment }

    val netWorth: Double
        get() = (cash +
                bank.depositBalance +
                totalCompanyValuation +
                totalStockValuation +
                totalCurrencyValuation +
                totalRealEstateValuation -
                bank.totalDebt).coerceAtLeast(0.0)

    val currentPrestige: Int
        get() = companies.sumOf { it.sector.prestigeScore * it.level } +
                (netWorth / 500_000.0).toInt() +
                achievements.count { it.isUnlocked } * 50

    fun calculateDailySummary(): DailySummaryFinancials {
        val hasCfo = executives.any { it.role == ExecutiveRole.CFO && it.isHired }
        val hasCoo = executives.any { it.role == ExecutiveRole.COO && it.isHired }
        val hasCmo = executives.any { it.role == ExecutiveRole.CMO && it.isHired }

        var totalGross = 0.0
        var totalCosts = 0.0
        var totalWages = 0.0
        var totalTaxes = 0.0

        for (company in companies) {
            val eventMultiplier = activeNewsEffects
                .filter { it.affectedSector == null || it.affectedSector == company.sector }
                .fold(1.0) { acc, event -> acc * event.impactMultiplier }

            val fin = company.calculateDailyFinancials(
                cfoBonus = hasCfo,
                cooBonus = hasCoo,
                cmoBonus = hasCmo,
                sectorEventMultiplier = eventMultiplier
            )

            totalGross += fin.grossRevenue
            totalCosts += (fin.materialCost + fin.rentCost)
            totalWages += fin.wageCost
            totalTaxes += fin.taxCost
        }

        val execSalaries = executives
            .filter { it.isHired }
            .sumOf { it.role.dailySalary }

        val depositInterest = bank.calculateDailyDepositInterest(economy.policyRate)
        val loanPayments = bank.totalDailyLoanPayments
        val realEstateRents = properties.sumOf { it.totalDailyRent }

        val netProfit = (totalGross + depositInterest + realEstateRents) -
                (totalCosts + totalWages + totalTaxes + execSalaries + loanPayments)

        return DailySummaryFinancials(
            grossCompanyRevenue = totalGross,
            companyOperatingCosts = totalCosts,
            companyWages = totalWages,
            companyTaxes = totalTaxes,
            executiveSalaries = execSalaries,
            bankDepositInterest = depositInterest,
            bankLoanPayments = loanPayments,
            realEstateRents = realEstateRents,
            netDailyProfit = netProfit
        )
    }

    val topCompetitors: List<CompetitorHolding>
        get() {
            val staticCompetitors = listOf(
                CompetitorHolding("c1", "Koçdemir Holding", "Mustafa Koçdemir", 14_500_000_000.0, "Sanayi & Otomotiv"),
                CompetitorHolding("c2", "Sabancıoğlu Grubu", "Güler Sabancıoğlu", 11_200_000_000.0, "Finans & Enerji"),
                CompetitorHolding("c3", "Eczacıgil Holding", "Bülent Eczacıgil", 8_400_000_000.0, "İlaç & Yapı"),
                CompetitorHolding("c4", "Yıldızhan Yatırım", "Murat Yıldızhan", 6_100_000_000.0, "Gıda & Perakende"),
                CompetitorHolding("c5", "Doğaner Medya & Enerji", "Aydın Doğaner", 4_300_000_000.0, "Medya & Petrol"),
                CompetitorHolding("c6", "Demirtaş Konsorsiyum", "Fuat Demirtaş", 2_800_000_000.0, "İnşaat & Lojistik"),
                CompetitorHolding("c7", "Kalyoncu Grup", "Cemal Kalyoncu", 1_650_000_000.0, "Altyapı & Havacılık"),
                CompetitorHolding("c8", "Akkökler Sanayi", "Raif Akkökler", 920_000_000.0, "Kimya & Elyaf"),
                CompetitorHolding("c9", "Zorlutepe Holding", "Ahmet Zorlutepe", 540_000_000.0, "Tekstil & Elektronik"),
                CompetitorHolding("c10", "Tekfenler Taahhüt", "Murat Tekfenler", 280_000_000.0, "Mühendislik & Tarım"),
                CompetitorHolding("c11", "Çelebican Yer Hizmetleri", "Can Çelebican", 120_000_000.0, "Havacılık Servisi"),
                CompetitorHolding("c12", "Genç KOBİ Girişimleri", "Bölgesel İş İnsanları", 25_000_000.0, "Ticaret"),
                CompetitorHolding("c13", "Yerel Perakendeciler Birliği", "Esnaf Odası", 2_500_000.0, "Perakende"),
                CompetitorHolding("c14", "Başlangıç Girişimcileri", "Yıldız Kuluçka", 250_000.0, "Startup")
            )

            val playerCompetitor = CompetitorHolding(
                id = "player",
                name = holdingName,
                founder = founderName,
                netWorth = netWorth,
                primarySector = companies.firstOrNull()?.sector?.title ?: "Yeni Başlayan",
                isPlayer = true
            )

            return (staticCompetitors + playerCompetitor).sortedByDescending { it.netWorth }
        }

    val playerRank: Int
        get() = topCompetitors.indexOfFirst { it.isPlayer } + 1
}

fun defaultStocks() = listOf(
    // 1. Perakende / Süpermarket Sektörü
    BistStock(
        ticker = "BIMAS",
        name = "BİM Birleşik Mağazalar",
        sectorName = "Süpermarket & Perakende",
        sector = Sector.MARKET,
        currentPrice = 524.50,
        openPrice = 518.00,
        dayHigh = 529.00,
        dayLow = 516.50,
        volume = 3_420_000L,
        priceHistory = listOf(498.0, 502.0, 510.0, 512.0, 518.0, 524.50),
        liveTicks = listOf(522.0, 523.0, 524.0, 524.50)
    ),
    BistStock(
        ticker = "MGROS",
        name = "Migros Ticaret A.Ş.",
        sectorName = "Süpermarket & Perakende",
        sector = Sector.MARKET,
        currentPrice = 465.00,
        openPrice = 461.50,
        dayHigh = 468.00,
        dayLow = 459.00,
        volume = 2_150_000L,
        priceHistory = listOf(445.0, 450.0, 456.0, 460.0, 461.5, 465.00),
        liveTicks = listOf(463.0, 464.0, 465.00)
    ),
    // 2. Restoran & Yeme-İçme Sektörü
    BistStock(
        ticker = "TABGD",
        name = "TAB Gıda Restoranları",
        sectorName = "Restoran & Kafe",
        sector = Sector.RESTAURANT,
        currentPrice = 142.80,
        openPrice = 140.20,
        dayHigh = 144.50,
        dayLow = 139.80,
        volume = 1_850_000L,
        priceHistory = listOf(134.0, 136.5, 138.0, 139.0, 140.2, 142.80),
        liveTicks = listOf(141.5, 142.0, 142.80)
    ),
    // 3. Lojistik & Kargo Sektörü
    BistStock(
        ticker = "THYIL",
        name = "Türk Havayolları Kargo & Ulaştırma",
        sectorName = "Lojistik & Taşımacılık",
        sector = Sector.LOGISTICS,
        currentPrice = 314.50,
        openPrice = 310.00,
        dayHigh = 318.00,
        dayLow = 308.50,
        volume = 8_940_000L,
        priceHistory = listOf(295.0, 299.0, 305.0, 302.0, 310.0, 314.50),
        liveTicks = listOf(312.0, 313.5, 314.50)
    ),
    BistStock(
        ticker = "RYSAS",
        name = "Reysaş Lojistik & Depoculuk",
        sectorName = "Lojistik & Taşımacılık",
        sector = Sector.LOGISTICS,
        currentPrice = 48.60,
        openPrice = 47.90,
        dayHigh = 49.50,
        dayLow = 47.50,
        volume = 1_420_000L,
        priceHistory = listOf(44.0, 45.2, 46.8, 47.2, 47.9, 48.60),
        liveTicks = listOf(48.2, 48.60)
    ),
    // 4. Sanayi & Üretim Sektörü
    BistStock(
        ticker = "TUPRS",
        name = "Tüpraş Petrol Rafinerileri",
        sectorName = "Sanayi & Üretim",
        sector = Sector.MANUFACTURING,
        currentPrice = 182.40,
        openPrice = 179.00,
        dayHigh = 184.00,
        dayLow = 178.50,
        volume = 6_200_000L,
        priceHistory = listOf(172.0, 174.0, 176.5, 180.0, 179.0, 182.40),
        liveTicks = listOf(181.0, 182.0, 182.40)
    ),
    BistStock(
        ticker = "EREGL",
        name = "Erdemir Demir Çelik Fabrikaları",
        sectorName = "Sanayi & Üretim",
        sector = Sector.MANUFACTURING,
        currentPrice = 52.30,
        openPrice = 51.80,
        dayHigh = 53.00,
        dayLow = 51.20,
        volume = 5_800_000L,
        priceHistory = listOf(48.5, 49.2, 50.4, 51.0, 51.8, 52.30),
        liveTicks = listOf(52.0, 52.30)
    ),
    // 5. İnşaat & Gayrimenkul Sektörü
    BistStock(
        ticker = "EKGYO",
        name = "Emlak Konut GYO",
        sectorName = "İnşaat & Gayrimenkul",
        sector = Sector.CONSTRUCTION,
        currentPrice = 12.85,
        openPrice = 12.50,
        dayHigh = 13.10,
        dayLow = 12.40,
        volume = 14_500_000L,
        priceHistory = listOf(11.2, 11.6, 12.0, 12.2, 12.5, 12.85),
        liveTicks = listOf(12.70, 12.85)
    ),
    BistStock(
        ticker = "ENKAI",
        name = "Enka İnşaat & Sanayi",
        sectorName = "İnşaat & Gayrimenkul",
        sector = Sector.CONSTRUCTION,
        currentPrice = 43.70,
        openPrice = 43.10,
        dayHigh = 44.20,
        dayLow = 42.80,
        volume = 2_600_000L,
        priceHistory = listOf(40.0, 41.2, 42.0, 42.8, 43.1, 43.70),
        liveTicks = listOf(43.5, 43.70)
    ),
    // 6. Teknoloji & Yazılım Sektörü
    BistStock(
        ticker = "ASELT",
        name = "Aselsan Elektronik & Savunma",
        sectorName = "Teknoloji, Yazılım & AI",
        sector = Sector.TECHNOLOGY,
        currentPrice = 67.40,
        openPrice = 64.50,
        dayHigh = 68.20,
        dayLow = 64.00,
        volume = 7_300_000L,
        priceHistory = listOf(59.0, 60.5, 62.0, 61.5, 64.5, 67.40),
        liveTicks = listOf(66.2, 67.0, 67.40)
    ),
    BistStock(
        ticker = "LOGO",
        name = "Logo Yazılım & Bulut Sistemleri",
        sectorName = "Teknoloji, Yazılım & AI",
        sector = Sector.TECHNOLOGY,
        currentPrice = 98.20,
        openPrice = 95.80,
        dayHigh = 99.50,
        dayLow = 95.00,
        volume = 1_100_000L,
        priceHistory = listOf(88.0, 90.5, 92.0, 94.0, 95.8, 98.20),
        liveTicks = listOf(97.5, 98.20)
    ),
    // 7. Turizm & Otelcilik Sektörü
    BistStock(
        ticker = "MAALT",
        name = "Marmaris Altınyunus Otelleri",
        sectorName = "Turizm, Otel & Tatil Köyü",
        sector = Sector.TOURISM,
        currentPrice = 780.00,
        openPrice = 765.00,
        dayHigh = 795.00,
        dayLow = 760.00,
        volume = 480_000L,
        priceHistory = listOf(720.0, 735.0, 745.0, 755.0, 765.0, 780.00),
        liveTicks = listOf(775.0, 780.00)
    ),
    BistStock(
        ticker = "AYCES",
        name = "Altınyunus Çeşme Turistik Tesisler",
        sectorName = "Turizm, Otel & Tatil Köyü",
        sector = Sector.TOURISM,
        currentPrice = 425.00,
        openPrice = 420.00,
        dayHigh = 432.00,
        dayLow = 418.00,
        volume = 320_000L,
        priceHistory = listOf(390.0, 400.0, 410.0, 415.0, 420.0, 425.00),
        liveTicks = listOf(422.0, 425.00)
    ),
    // 8. Finans & Bankacılık Sektörü
    BistStock(
        ticker = "AKBNK",
        name = "Akbank T.A.Ş.",
        sectorName = "Yatırım Bankacılığı & Finans",
        sector = Sector.FINANCE,
        currentPrice = 61.20,
        openPrice = 59.80,
        dayHigh = 62.00,
        dayLow = 59.50,
        volume = 12_800_000L,
        priceHistory = listOf(54.0, 55.2, 56.5, 57.8, 59.8, 61.20),
        liveTicks = listOf(60.8, 61.20)
    ),
    BistStock(
        ticker = "GARAN",
        name = "Garanti BBVA Bankası",
        sectorName = "Yatırım Bankacılığı & Finans",
        sector = Sector.FINANCE,
        currentPrice = 118.50,
        openPrice = 116.00,
        dayHigh = 120.00,
        dayLow = 115.50,
        volume = 9_400_000L,
        priceHistory = listOf(105.0, 108.0, 110.5, 114.0, 116.0, 118.50),
        liveTicks = listOf(117.8, 118.50)
    ),
    // 9. BIST Sektör Endeks Fonu
    BistStock(
        ticker = "XSEKTR",
        name = "BIST 100 Sektörler Karma Fonu",
        sectorName = "Sektör Endeks Fonu (ETF)",
        sector = null,
        currentPrice = 250.00,
        openPrice = 246.50,
        dayHigh = 253.00,
        dayLow = 245.00,
        volume = 18_200_000L,
        priceHistory = listOf(230.0, 235.0, 240.0, 242.0, 246.5, 250.00),
        liveTicks = listOf(249.0, 250.00)
    )
)

fun defaultCurrencies() = listOf(
    CurrencyAsset(
        code = "USD",
        name = "Amerikan Doları",
        symbol = "$",
        currentRate = 34.50,
        openRate = 34.42,
        history = listOf(33.80, 34.05, 34.20, 34.35, 34.42, 34.50)
    ),
    CurrencyAsset(
        code = "EUR",
        name = "Euro",
        symbol = "€",
        currentRate = 37.80,
        openRate = 37.70,
        history = listOf(36.90, 37.20, 37.40, 37.55, 37.70, 37.80)
    ),
    CurrencyAsset(
        code = "GOLD",
        name = "Gram Altın",
        symbol = "Au",
        currentRate = 3020.0,
        openRate = 3005.0,
        history = listOf(2920.0, 2950.0, 2980.0, 2995.0, 3005.0, 3020.0)
    )
)

fun defaultProperties() = listOf(
    RealEstateProperty(
        id = "prop_levent",
        name = "Levent Plaza Ofis Katı",
        location = "İstanbul, Levent",
        purchasePrice = 8_500_000.0,
        dailyRentIncome = 6_500.0,
        ownedCount = 0
    ),
    RealEstateProperty(
        id = "prop_nisantasi",
        name = "Nişantaşı Lüks Rezidans",
        location = "İstanbul, Şişli",
        purchasePrice = 14_000_000.0,
        dailyRentIncome = 11_500.0,
        ownedCount = 0
    ),
    RealEstateProperty(
        id = "prop_kocaeli",
        name = "Kocaeli Lojistik Antrepo & Depo",
        location = "Kocaeli, Gebze",
        purchasePrice = 28_000_000.0,
        dailyRentIncome = 24_000.0,
        ownedCount = 0
    ),
    RealEstateProperty(
        id = "prop_maslak",
        name = "Maslak İş Kuleleri Kompleksi",
        location = "İstanbul, Maslak",
        purchasePrice = 75_000_000.0,
        dailyRentIncome = 68_000.0,
        ownedCount = 0
    )
)

fun defaultExecutives() = listOf(
    ExecutiveState(ExecutiveRole.CFO),
    ExecutiveState(ExecutiveRole.COO),
    ExecutiveState(ExecutiveRole.CMO),
    ExecutiveState(ExecutiveRole.CTO)
)

fun defaultInitialNews() = listOf(
    NewsEvent(
        id = "news_init_1",
        day = 1,
        title = "Piyasalarda Bahar İyimserliği: Yeni Girişimler Destekleniyor",
        content = "Türkiye ekonomisinde yerli sermaye yatırımları hız kazandı. Girişimcilerin kurduğu ilk işletmeler için vergi ve bürokrasi kolaylığı sağlandı.",
        category = "Piyasalar"
    ),
    NewsEvent(
        id = "news_init_2",
        day = 1,
        title = "TCMB Politika Faizini %48 Seviyesinde Sabit Tuttu",
        content = "Merkez Bankası enflasyonla mücadele kapsamında sıkı para politikası duruşunu koruyor. Mevduat getirileri cazibesini korurken kredi maliyetleri dengelendi.",
        category = "TCMB"
    )
)

fun defaultAchievements() = listOf(
    Achievement(
        id = "ach_first_biz",
        title = "İlk Adım: Esnaflık",
        description = "Holding bünyesinde ilk şirketini kur.",
        rewardTry = 25_000.0,
        targetLabel = "1 Şirket"
    ),
    Achievement(
        id = "ach_millionaire",
        title = "Milyonerler Kulübü",
        description = "Holding toplam net değerini 1.000.000 TL üzerine çıkar.",
        rewardTry = 100_000.0,
        targetLabel = "₺1.000.000"
    ),
    Achievement(
        id = "ach_employer",
        title = "Büyük İstihdam",
        description = "Holding şirketlerinde toplam 50 çalışana ulaş.",
        rewardTry = 75_000.0,
        targetLabel = "50 Çalışan"
    ),
    Achievement(
        id = "ach_conglomerate",
        title = "Çok Sektörlü İmparatorluk",
        description = "En az 4 farklı sektörde faal şirket sahibi ol.",
        rewardTry = 250_000.0,
        targetLabel = "4 Sektör"
    ),
    Achievement(
        id = "ach_bist_trader",
        title = "Borsa Kurdu",
        description = "Hisse senetlerinde 500.000 TL değerinde portföy oluştur.",
        rewardTry = 150_000.0,
        targetLabel = "₺500.000 Portföy"
    ),
    Achievement(
        id = "ach_billionaire",
        title = "Milyarder Vizyonu",
        description = "Holding değerini 100.000.000 TL barajının üzerine taşı.",
        rewardTry = 2_500_000.0,
        targetLabel = "₺100.000.000"
    ),
    Achievement(
        id = "ach_top_ten",
        title = "Forbes Türkiye İlk 10",
        description = "Türkiye'nin en zengin 10 holdingi arasına gir.",
        rewardTry = 10_000_000.0,
        targetLabel = "Forbes Top 10"
    )
)
