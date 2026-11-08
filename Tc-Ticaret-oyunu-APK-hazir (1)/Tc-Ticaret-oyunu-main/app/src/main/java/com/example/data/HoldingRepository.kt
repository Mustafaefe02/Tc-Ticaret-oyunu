package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class HoldingRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("holding_sim_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(loadInitialState())
    val gameState: StateFlow<HoldingGameState> = _gameState.asStateFlow()

    private var daysSinceLastNews = 0

    private fun loadInitialState(): HoldingGameState {
        val savedJson = prefs.getString("saved_game_state", null)
        if (savedJson != null) {
            try {
                return parseJsonToState(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return HoldingGameState()
    }

    fun saveCurrentState() {
        try {
            val json = serializeStateToJson(_gameState.value)
            prefs.edit().putString("saved_game_state", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetGame(customHoldingName: String? = null) {
        val name = customHoldingName?.trim().orEmpty()
        val newState = HoldingGameState(
            holdingName = name,
            isHoldingNameCustomized = name.isNotBlank(),
            cash = 50_000.0
        )
        _gameState.value = newState
        saveCurrentState()
    }

    fun updateHoldingName(newName: String, founderName: String? = null) {
        val trimmed = newName.trim()
        val current = _gameState.value
        _gameState.value = current.copy(
            holdingName = trimmed,
            founderName = founderName?.trim()?.ifBlank { current.founderName } ?: current.founderName,
            isHoldingNameCustomized = trimmed.isNotBlank()
        )
        saveCurrentState()
    }

    // --- GAME ENGINE: ADVANCE DAY ---
    fun advanceDay() {
        val current = _gameState.value
        val nextDay = current.economy.day + 1
        daysSinceLastNews++

        // 1. Process Active News and potential new news event
        val updatedActiveNews = current.activeNewsEffects.mapNotNull { event ->
            val remaining = event.durationDays - 1
            if (remaining > 0) event.copy(durationDays = remaining) else null
        }.toMutableList()

        val updatedNewsHistory = current.newsHistory.toMutableList()

        // News generation chance
        var newEventTriggered: NewsEvent? = null
        if (daysSinceLastNews >= 7 && Random.nextFloat() < 0.35f) {
            val generatedEvent = generateRandomNewsEvent(nextDay)
            daysSinceLastNews = 0
            newEventTriggered = generatedEvent
            updatedNewsHistory.add(0, generatedEvent)
            if (generatedEvent.impactMultiplier != 1.0) {
                updatedActiveNews.add(generatedEvent)
            }
        }

        // 2. Economy & Market Price Simulation
        val newSentiment = determineSentiment(current.economy.sentiment, newEventTriggered)
        val sentimentBias = when (newSentiment) {
            MarketSentiment.BULLISH -> 0.012
            MarketSentiment.BEARISH -> -0.015
            MarketSentiment.VOLATILE -> 0.002
            MarketSentiment.STABLE -> 0.004
        }

        // Currencies
        val newUsd = (current.economy.usdTry * (1.0 + (Random.nextDouble(-0.008, 0.015) + sentimentBias * 0.5))).coerceIn(28.0, 95.0)
        val newEur = (current.economy.eurTry * (1.0 + (Random.nextDouble(-0.008, 0.014) + sentimentBias * 0.4))).coerceIn(30.0, 105.0)
        val newGold = (current.economy.goldGramTry * (1.0 + (Random.nextDouble(-0.012, 0.02) + sentimentBias * 0.6))).coerceIn(2400.0, 9900.0)

        val updatedCurrencies = current.currencies.map { cur ->
            val rate = when (cur.code) {
                "USD" -> newUsd
                "EUR" -> newEur
                else -> newGold
            }
            val newHist = (cur.history + rate).takeLast(14)
            cur.copy(
                openRate = cur.currentRate,
                currentRate = rate,
                history = newHist
            )
        }

        // Stocks
        val updatedStocks = current.stocks.map { stock ->
            val randomFactor = Random.nextDouble(-0.035, 0.04) + sentimentBias
            val price = (stock.currentPrice * (1.0 + randomFactor)).coerceAtLeast(1.0)
            val newHist = (stock.priceHistory + price).takeLast(14)
            stock.copy(
                openPrice = stock.currentPrice,
                currentPrice = price,
                priceHistory = newHist
            )
        }

        val bistChange = updatedStocks.map { it.dailyChangePercent }.average()
        val newBist = (current.economy.bist100Index * (1.0 + bistChange / 100.0)).coerceAtLeast(1000.0)

        var newPolicyRate = current.economy.policyRate
        var newInflation = current.economy.inflationAnnual
        if (newEventTriggered?.category == "TCMB") {
            if (newEventTriggered.title.contains("İndirildi")) {
                newPolicyRate = (newPolicyRate - 1.5).coerceAtLeast(10.0)
            } else if (newEventTriggered.title.contains("Artırıldı")) {
                newPolicyRate = (newPolicyRate + 1.5).coerceAtMost(70.0)
            }
        } else if (nextDay % 30 == 0) {
            newInflation = (newInflation + Random.nextDouble(-1.2, 1.5)).coerceIn(15.0, 85.0)
        }

        val updatedEconomy = current.economy.copy(
            day = nextDay,
            usdTry = newUsd,
            eurTry = newEur,
            goldGramTry = newGold,
            inflationAnnual = newInflation,
            policyRate = newPolicyRate,
            bist100Index = newBist,
            sentiment = newSentiment
        )

        // 3. Companies Updates
        val hasCfo = current.executives.any { it.role == ExecutiveRole.CFO && it.isHired }
        val hasCoo = current.executives.any { it.role == ExecutiveRole.COO && it.isHired }
        val hasCmo = current.executives.any { it.role == ExecutiveRole.CMO && it.isHired }

        val updatedCompanies = current.companies.map { comp ->
            val eventMultiplier = updatedActiveNews
                .filter { it.affectedSector == null || it.affectedSector == comp.sector }
                .fold(1.0) { acc, event -> acc * event.impactMultiplier }

            val fin = comp.calculateDailyFinancials(hasCfo, hasCoo, hasCmo, eventMultiplier)
            val newMarketing = (comp.marketingDaysLeft - 1).coerceAtLeast(0)

            comp.copy(
                marketingDaysLeft = newMarketing,
                totalRevenueEarned = comp.totalRevenueEarned + fin.grossRevenue
            )
        }

        // 4. Financial Calculations & Cashflow
        val dailySummary = current.calculateDailySummary()
        var newCash = current.cash + dailySummary.netDailyProfit

        // Bank updates (Mevduat & Loans)
        var newCreditScore = current.bank.creditScore
        val updatedLoans = mutableListOf<ActiveLoan>()
        for (loan in current.bank.activeLoans) {
            val newDays = loan.daysRemaining - 1
            val newBalance = (loan.remainingBalance - (loan.dailyPayment * 0.85)).coerceAtLeast(0.0)
            if (newDays > 0 && newBalance > 1.0) {
                updatedLoans.add(loan.copy(remainingBalance = newBalance, daysRemaining = newDays))
            } else {
                // Loan completed! Credit score boost
                newCreditScore = (newCreditScore + 35).coerceAtMost(1900)
            }
        }

        val updatedBank = current.bank.copy(
            depositBalance = current.bank.depositBalance + dailySummary.bankDepositInterest,
            activeLoans = updatedLoans,
            creditScore = newCreditScore
        )

        // 5. Achievements check
        val tempStateForCheck = current.copy(
            cash = newCash,
            companies = updatedCompanies,
            stocks = updatedStocks,
            currencies = updatedCurrencies,
            bank = updatedBank,
            economy = updatedEconomy
        )

        val updatedAchievements = checkAchievements(tempStateForCheck)
        // Add rewards for newly unlocked achievements
        for (ach in updatedAchievements) {
            val wasUnlocked = current.achievements.find { it.id == ach.id }?.isUnlocked == true
            if (ach.isUnlocked && !wasUnlocked) {
                newCash += ach.rewardTry
            }
        }

        val finalState = tempStateForCheck.copy(
            cash = newCash,
            activeNewsEffects = updatedActiveNews,
            newsHistory = updatedNewsHistory.take(20),
            achievements = updatedAchievements
        )

        _gameState.value = finalState
        if (nextDay % 2 == 0) {
            saveCurrentState()
        }
    }

    // --- REAL-TIME MARKET PRICE SIMULATION ---
    fun tickLiveMarketPrices() {
        val current = _gameState.value
        val sentimentBias = when (current.economy.sentiment) {
            MarketSentiment.BULLISH -> 0.0015
            MarketSentiment.BEARISH -> -0.0018
            MarketSentiment.VOLATILE -> 0.0002
            MarketSentiment.STABLE -> 0.0004
        }

        val updatedStocks = current.stocks.map { stock ->
            val pctDelta = Random.nextDouble(-0.0022, 0.0025) + sentimentBias
            val rawPrice = stock.currentPrice * (1.0 + pctDelta)
            val newPrice = (Math.round(rawPrice * 100.0) / 100.0).coerceAtLeast(1.0)

            val direction = when {
                newPrice > stock.currentPrice -> 1
                newPrice < stock.currentPrice -> -1
                else -> 0
            }

            val newHigh = maxOf(stock.dayHigh, newPrice)
            val newLow = minOf(stock.dayLow, newPrice)
            val newVolume = stock.volume + Random.nextLong(150, 3200)
            val newLiveTicks = (stock.liveTicks + newPrice).takeLast(28)

            stock.copy(
                currentPrice = newPrice,
                dayHigh = newHigh,
                dayLow = newLow,
                volume = newVolume,
                liveTicks = newLiveTicks,
                lastTickDirection = direction
            )
        }

        val updatedCurrencies = current.currencies.map { cur ->
            val curDelta = Random.nextDouble(-0.0005, 0.0006)
            val newRate = Math.round((cur.currentRate * (1.0 + curDelta)) * 100.0) / 100.0
            cur.copy(currentRate = newRate)
        }

        _gameState.value = current.copy(
            stocks = updatedStocks,
            currencies = updatedCurrencies
        )
    }

    // --- COMPANY MANAGEMENT ACTIONS ---
    fun foundCompany(name: String, sector: Sector): Boolean {
        val current = _gameState.value
        if (current.cash < sector.startingCost) return false

        val newCompany = Company(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "${current.holdingName} ${sector.title}" },
            sector = sector,
            level = 1,
            staffCount = sector.baseStaff,
            foundedDay = current.economy.day
        )

        val updatedCash = current.cash - sector.startingCost
        val updatedCompanies = current.companies + newCompany
        _gameState.value = current.copy(cash = updatedCash, companies = updatedCompanies)
        saveCurrentState()
        return true
    }

    fun upgradeCompanyLevel(companyId: String): Boolean {
        val current = _gameState.value
        val company = current.companies.find { it.id == companyId } ?: return false
        val cost = company.upgradeCost
        if (current.cash < cost) return false

        val updatedCompanies = current.companies.map {
            if (it.id == companyId) {
                val newLevel = it.level + 1
                it.copy(
                    level = newLevel,
                    staffCount = it.staffCount + it.sector.baseStaff
                )
            } else it
        }

        _gameState.value = current.copy(cash = current.cash - cost, companies = updatedCompanies)
        saveCurrentState()
        return true
    }

    fun adjustCompanyStaff(companyId: String, delta: Int): Boolean {
        val current = _gameState.value
        val company = current.companies.find { it.id == companyId } ?: return false
        val targetStaff = (company.staffCount + delta).coerceIn(1, company.sector.baseStaff * company.level * 3)

        val updatedCompanies = current.companies.map {
            if (it.id == companyId) it.copy(staffCount = targetStaff) else it
        }
        _gameState.value = current.copy(companies = updatedCompanies)
        saveCurrentState()
        return true
    }

    fun setCompanyPricingStrategy(companyId: String, strategy: PricingStrategy) {
        val current = _gameState.value
        val updatedCompanies = current.companies.map {
            if (it.id == companyId) it.copy(pricingStrategy = strategy) else it
        }
        _gameState.value = current.copy(companies = updatedCompanies)
        saveCurrentState()
    }

    fun startMarketingCampaign(companyId: String, campaignType: Int): Boolean {
        val current = _gameState.value
        val company = current.companies.find { it.id == companyId } ?: return false

        // 1: Dijital Reklam (30k TL, 15 gün), 2: TV & Billboard (100k TL, 30 gün)
        val hasCmo = current.executives.any { it.role == ExecutiveRole.CMO && it.isHired }
        val cost = if (campaignType == 1) 35_000.0 else 120_000.0
        val days = (if (campaignType == 1) 15 else 30) * (if (hasCmo) 1.5 else 1.0).toInt()

        if (current.cash < cost) return false

        val updatedCompanies = current.companies.map {
            if (it.id == companyId) it.copy(marketingDaysLeft = it.marketingDaysLeft + days) else it
        }

        _gameState.value = current.copy(cash = current.cash - cost, companies = updatedCompanies)
        saveCurrentState()
        return true
    }

    fun upgradeAutomation(companyId: String): Boolean {
        val current = _gameState.value
        val company = current.companies.find { it.id == companyId } ?: return false
        val hasCto = current.executives.any { it.role == ExecutiveRole.CTO && it.isHired }
        val cost = company.automationUpgradeCost * (if (hasCto) 0.50 else 1.0)

        if (current.cash < cost) return false

        val updatedCompanies = current.companies.map {
            if (it.id == companyId) it.copy(automationLevel = it.automationLevel + 1) else it
        }

        _gameState.value = current.copy(cash = current.cash - cost, companies = updatedCompanies)
        saveCurrentState()
        return true
    }

    fun sellCompany(companyId: String): Double {
        val current = _gameState.value
        val company = current.companies.find { it.id == companyId } ?: return 0.0
        val saleProceeds = company.valuation * 0.90 // 10% transaction fee

        val updatedCompanies = current.companies.filterNot { it.id == companyId }
        _gameState.value = current.copy(
            cash = current.cash + saleProceeds,
            companies = updatedCompanies
        )
        saveCurrentState()
        return saleProceeds
    }

    // --- STOCK MARKET ACTIONS ---
    fun buyStock(ticker: String, shares: Int): Boolean {
        val current = _gameState.value
        val stock = current.stocks.find { it.ticker == ticker } ?: return false
        val totalCost = stock.currentPrice * shares
        if (current.cash < totalCost || shares <= 0) return false

        val currentTotalPaid = stock.ownedShares * stock.avgPurchasePrice
        val newShares = stock.ownedShares + shares
        val newAvg = (currentTotalPaid + totalCost) / newShares

        val updatedStocks = current.stocks.map {
            if (it.ticker == ticker) it.copy(ownedShares = newShares, avgPurchasePrice = newAvg) else it
        }

        _gameState.value = current.copy(cash = current.cash - totalCost, stocks = updatedStocks)
        saveCurrentState()
        return true
    }

    fun sellStock(ticker: String, shares: Int): Boolean {
        val current = _gameState.value
        val stock = current.stocks.find { it.ticker == ticker } ?: return false
        if (stock.ownedShares < shares || shares <= 0) return false

        val proceeds = stock.currentPrice * shares
        val remainingShares = stock.ownedShares - shares
        val newAvg = if (remainingShares == 0) 0.0 else stock.avgPurchasePrice

        val updatedStocks = current.stocks.map {
            if (it.ticker == ticker) it.copy(ownedShares = remainingShares, avgPurchasePrice = newAvg) else it
        }

        _gameState.value = current.copy(cash = current.cash + proceeds, stocks = updatedStocks)
        saveCurrentState()
        return true
    }

    // --- CURRENCY & GOLD ACTIONS ---
    fun buyCurrency(code: String, amount: Double): Boolean {
        val current = _gameState.value
        val cur = current.currencies.find { it.code == code } ?: return false
        val costTry = cur.currentRate * amount
        if (current.cash < costTry || amount <= 0) return false

        val currentTotalSpent = cur.ownedAmount * cur.avgBuyRate
        val newAmount = cur.ownedAmount + amount
        val newAvg = (currentTotalSpent + costTry) / newAmount

        val updatedCurrencies = current.currencies.map {
            if (it.code == code) it.copy(ownedAmount = newAmount, avgBuyRate = newAvg) else it
        }

        _gameState.value = current.copy(cash = current.cash - costTry, currencies = updatedCurrencies)
        saveCurrentState()
        return true
    }

    fun sellCurrency(code: String, amount: Double): Boolean {
        val current = _gameState.value
        val cur = current.currencies.find { it.code == code } ?: return false
        if (cur.ownedAmount < amount || amount <= 0) return false

        val proceedsTry = cur.currentRate * amount
        val remainingAmount = cur.ownedAmount - amount
        val newAvg = if (remainingAmount <= 0.001) 0.0 else cur.avgBuyRate

        val updatedCurrencies = current.currencies.map {
            if (it.code == code) it.copy(ownedAmount = remainingAmount, avgBuyRate = newAvg) else it
        }

        _gameState.value = current.copy(cash = current.cash + proceedsTry, currencies = updatedCurrencies)
        saveCurrentState()
        return true
    }

    // --- REAL ESTATE ACTIONS ---
    fun buyProperty(propertyId: String): Boolean {
        val current = _gameState.value
        val prop = current.properties.find { it.id == propertyId } ?: return false
        if (current.cash < prop.purchasePrice) return false

        val updatedProperties = current.properties.map {
            if (it.id == propertyId) it.copy(ownedCount = it.ownedCount + 1) else it
        }

        _gameState.value = current.copy(
            cash = current.cash - prop.purchasePrice,
            properties = updatedProperties
        )
        saveCurrentState()
        return true
    }

    // --- BANKING ACTIONS ---
    fun depositToBank(amount: Double): Boolean {
        val current = _gameState.value
        if (amount <= 0 || current.cash < amount) return false

        val updatedBank = current.bank.copy(depositBalance = current.bank.depositBalance + amount)
        _gameState.value = current.copy(cash = current.cash - amount, bank = updatedBank)
        saveCurrentState()
        return true
    }

    fun withdrawFromBank(amount: Double): Boolean {
        val current = _gameState.value
        if (amount <= 0 || current.bank.depositBalance < amount) return false

        val updatedBank = current.bank.copy(depositBalance = current.bank.depositBalance - amount)
        _gameState.value = current.copy(cash = current.cash + amount, bank = updatedBank)
        saveCurrentState()
        return true
    }

    fun takeLoan(loanType: Int): Boolean {
        val current = _gameState.value
        val hasCfo = current.executives.any { it.role == ExecutiveRole.CFO && it.isHired }
        val interestDiscount = if (hasCfo) 0.85 else 1.0

        // Loan presets
        val (name, principal, days, baseRate) = when (loanType) {
            1 -> Quadruple("KOBİ Destek Kredisi", 150_000.0, 60, 48.0)
            2 -> Quadruple("Holding Büyüme Finansmanı", 1_000_000.0, 120, 44.0)
            else -> Quadruple("Sendikasyon Yatırım Kredisi", 10_000_000.0, 240, 40.0)
        }

        // Requirements check based on net worth & credit score
        if (loanType == 2 && current.netWorth < 500_000.0) return false
        if (loanType == 3 && (current.netWorth < 5_000_000.0 || current.bank.creditScore < 1400)) return false

        val effectiveRate = baseRate * interestDiscount
        val totalRepay = principal * (1.0 + (effectiveRate / 100.0) * (days / 365.0))
        val dailyPayment = totalRepay / days

        val newLoan = ActiveLoan(
            id = UUID.randomUUID().toString(),
            name = name,
            principalAmount = principal,
            remainingBalance = totalRepay,
            dailyPayment = dailyPayment,
            daysRemaining = days,
            interestRateAnnual = effectiveRate
        )

        val updatedBank = current.bank.copy(
            activeLoans = current.bank.activeLoans + newLoan,
            creditScore = (current.bank.creditScore - 15).coerceAtLeast(1000)
        )

        _gameState.value = current.copy(
            cash = current.cash + principal,
            bank = updatedBank
        )
        saveCurrentState()
        return true
    }

    // --- EXECUTIVE ACTIONS ---
    fun hireExecutive(role: ExecutiveRole): Boolean {
        val current = _gameState.value
        val exec = current.executives.find { it.role == role } ?: return false
        if (exec.isHired || current.cash < role.hiringCost) return false

        val updatedExecutives = current.executives.map {
            if (it.role == role) it.copy(isHired = true, hiredDay = current.economy.day) else it
        }

        _gameState.value = current.copy(
            cash = current.cash - role.hiringCost,
            executives = updatedExecutives
        )
        saveCurrentState()
        return true
    }

    fun fireExecutive(role: ExecutiveRole) {
        val current = _gameState.value
        val updatedExecutives = current.executives.map {
            if (it.role == role) it.copy(isHired = false) else it
        }
        _gameState.value = current.copy(executives = updatedExecutives)
        saveCurrentState()
    }

    // --- HELPER LOGIC ---
    private fun determineSentiment(current: MarketSentiment, event: NewsEvent?): MarketSentiment {
        if (event != null) {
            return when {
                event.category == "Kriz" -> MarketSentiment.BEARISH
                event.category == "Fırsat" -> MarketSentiment.BULLISH
                event.category == "TCMB" && event.title.contains("İndirildi") -> MarketSentiment.BULLISH
                else -> current
            }
        }
        val roll = Random.nextFloat()
        return when {
            roll < 0.10f -> MarketSentiment.BULLISH
            roll < 0.20f -> MarketSentiment.BEARISH
            roll < 0.35f -> MarketSentiment.VOLATILE
            else -> MarketSentiment.STABLE
        }
    }

    private fun generateRandomNewsEvent(day: Int): NewsEvent {
        val pool = listOf(
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Turizm Sezonunda Patlama: Otellerde Rezervasyon Rekoru",
                content = "Yurt dışından gelen turist sayısındaki rekor artışla turizm ve yeme-içme sektörü bayram ediyor. Turizm gelirleri %35 artış gösterdi.",
                category = "Fırsat",
                affectedSector = Sector.TOURISM,
                impactMultiplier = 1.35,
                durationDays = 20
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Sanayi Üretimi İhracat Hamlesiyle Uçuşa Geçti",
                content = "Avrupa ve Körfez ülkelerinden gelen siparişlerle sanayi ve üretim fabrikaları kapasitelerini %100'e çıkardı.",
                category = "Fırsat",
                affectedSector = Sector.MANUFACTURING,
                impactMultiplier = 1.25,
                durationDays = 25
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "TCMB Sürpriz Kararla Faizleri %1.5 İndirdi",
                content = "Merkez Bankası piyasa beklentilerinin ötesinde faiz indirimine gitti. BIST 100 endeksi coşkuyla yükselirken kredi maliyetleri düştü.",
                category = "TCMB",
                impactMultiplier = 1.10,
                durationDays = 15
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Yapay Zeka & Yazılım Girişimlerine Dev Devlet Hibesi",
                content = "Sanayi ve Teknoloji Bakanlığı teknoloji şirketlerine yönelik 10 Milyar TL'lik yeni Ar-Ge teşvik paketini onayladı.",
                category = "Fırsat",
                affectedSector = Sector.TECHNOLOGY,
                impactMultiplier = 1.40,
                durationDays = 30
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Küresel Tedarik Zinciri Krizi: Lojistik Maliyetleri Arttı",
                content = "Boğazlar ve uluslararası nakliye hatlarındaki tıkanıklık navlun fiyatlarını yükseltti. Lojistik ve perakende kârları geçici olarak baskılandı.",
                category = "Kriz",
                affectedSector = Sector.LOGISTICS,
                impactMultiplier = 0.85,
                durationDays = 18
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Konut & Gayrimenkul Talebinde Büyük Canlanma",
                content = "Yeni kentsel dönüşüm teşvikleriyle birlikte plaza ve gayrimenkul projelerine ilgi tavan yaptı.",
                category = "Fırsat",
                affectedSector = Sector.CONSTRUCTION,
                impactMultiplier = 1.30,
                durationDays = 20
            ),
            NewsEvent(
                id = UUID.randomUUID().toString(),
                day = day,
                title = "Gıda ve Temel Tüketim Ürünlerine Talep Zirvede",
                content = "Vatandaşların temel ihtiyaç harcamaları süpermarket ve perakende zincirlerinin cirolarını yukarı taşıdı.",
                category = "Piyasalar",
                affectedSector = Sector.MARKET,
                impactMultiplier = 1.20,
                durationDays = 15
            )
        )
        return pool.random()
    }

    private fun checkAchievements(state: HoldingGameState): List<Achievement> {
        return state.achievements.map { ach ->
            if (ach.isUnlocked) return@map ach

            val (unlocked, progress) = when (ach.id) {
                "ach_first_biz" -> Pair(state.companies.isNotEmpty(), if (state.companies.isNotEmpty()) 1f else 0f)
                "ach_millionaire" -> Pair(state.netWorth >= 1_000_000.0, (state.netWorth / 1_000_000.0).toFloat().coerceIn(0f, 1f))
                "ach_employer" -> Pair(state.totalEmployees >= 50, (state.totalEmployees / 50f).coerceIn(0f, 1f))
                "ach_conglomerate" -> {
                    val distinctSectors = state.companies.map { it.sector }.distinct().size
                    Pair(distinctSectors >= 4, (distinctSectors / 4f).coerceIn(0f, 1f))
                }
                "ach_bist_trader" -> {
                    val stockVal = state.totalStockValuation
                    Pair(stockVal >= 500_000.0, (stockVal / 500_000.0).toFloat().coerceIn(0f, 1f))
                }
                "ach_billionaire" -> Pair(state.netWorth >= 100_000_000.0, (state.netWorth / 100_000_000.0).toFloat().coerceIn(0f, 1f))
                "ach_top_ten" -> Pair(state.playerRank <= 10, (1f - ((state.playerRank - 10).coerceAtLeast(0) / 10f)).coerceIn(0f, 1f))
                else -> Pair(false, 0f)
            }

            ach.copy(isUnlocked = unlocked, progress = progress)
        }
    }

    // --- JSON SERIALIZATION ---
    private fun serializeStateToJson(state: HoldingGameState): String {
        val root = JSONObject()
        root.put("holdingName", state.holdingName)
        root.put("founderName", state.founderName)
        root.put("isHoldingNameCustomized", state.isHoldingNameCustomized)
        root.put("cash", state.cash)

        // Companies
        val compArr = JSONArray()
        for (c in state.companies) {
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("name", c.name)
            cObj.put("sector", c.sector.name)
            cObj.put("level", c.level)
            cObj.put("staffCount", c.staffCount)
            cObj.put("pricingStrategy", c.pricingStrategy.name)
            cObj.put("marketingDaysLeft", c.marketingDaysLeft)
            cObj.put("automationLevel", c.automationLevel)
            cObj.put("totalRevenueEarned", c.totalRevenueEarned)
            cObj.put("foundedDay", c.foundedDay)
            compArr.put(cObj)
        }
        root.put("companies", compArr)

        // Bank
        val bObj = JSONObject()
        bObj.put("depositBalance", state.bank.depositBalance)
        bObj.put("creditScore", state.bank.creditScore)
        val loansArr = JSONArray()
        for (l in state.bank.activeLoans) {
            val lObj = JSONObject()
            lObj.put("id", l.id)
            lObj.put("name", l.name)
            lObj.put("principalAmount", l.principalAmount)
            lObj.put("remainingBalance", l.remainingBalance)
            lObj.put("dailyPayment", l.dailyPayment)
            lObj.put("daysRemaining", l.daysRemaining)
            lObj.put("interestRateAnnual", l.interestRateAnnual)
            loansArr.put(lObj)
        }
        bObj.put("activeLoans", loansArr)
        root.put("bank", bObj)

        // Economy
        val eObj = JSONObject()
        eObj.put("day", state.economy.day)
        eObj.put("usdTry", state.economy.usdTry)
        eObj.put("eurTry", state.economy.eurTry)
        eObj.put("goldGramTry", state.economy.goldGramTry)
        eObj.put("inflationAnnual", state.economy.inflationAnnual)
        eObj.put("policyRate", state.economy.policyRate)
        eObj.put("bist100Index", state.economy.bist100Index)
        eObj.put("sentiment", state.economy.sentiment.name)
        root.put("economy", eObj)

        // Stocks
        val stArr = JSONArray()
        for (s in state.stocks) {
            val sObj = JSONObject()
            sObj.put("ticker", s.ticker)
            sObj.put("name", s.name)
            sObj.put("sectorName", s.sectorName)
            sObj.put("sector", s.sector?.name ?: "")
            sObj.put("currentPrice", s.currentPrice)
            sObj.put("openPrice", s.openPrice)
            sObj.put("dayHigh", s.dayHigh)
            sObj.put("dayLow", s.dayLow)
            sObj.put("volume", s.volume)
            sObj.put("ownedShares", s.ownedShares)
            sObj.put("avgPurchasePrice", s.avgPurchasePrice)
            val hArr = JSONArray()
            s.priceHistory.forEach { hArr.put(it) }
            sObj.put("priceHistory", hArr)
            stArr.put(sObj)
        }
        root.put("stocks", stArr)

        // Currencies
        val curArr = JSONArray()
        for (cur in state.currencies) {
            val curObj = JSONObject()
            curObj.put("code", cur.code)
            curObj.put("name", cur.name)
            curObj.put("symbol", cur.symbol)
            curObj.put("currentRate", cur.currentRate)
            curObj.put("openRate", cur.openRate)
            curObj.put("ownedAmount", cur.ownedAmount)
            curObj.put("avgBuyRate", cur.avgBuyRate)
            val hArr = JSONArray()
            cur.history.forEach { hArr.put(it) }
            curObj.put("history", hArr)
            curArr.put(curObj)
        }
        root.put("currencies", curArr)

        // Properties
        val pArr = JSONArray()
        for (p in state.properties) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("location", p.location)
            pObj.put("purchasePrice", p.purchasePrice)
            pObj.put("dailyRentIncome", p.dailyRentIncome)
            pObj.put("ownedCount", p.ownedCount)
            pArr.put(pObj)
        }
        root.put("properties", pArr)

        // Executives
        val exArr = JSONArray()
        for (ex in state.executives) {
            val exObj = JSONObject()
            exObj.put("role", ex.role.name)
            exObj.put("isHired", ex.isHired)
            exObj.put("hiredDay", ex.hiredDay)
            exArr.put(exObj)
        }
        root.put("executives", exArr)

        return root.toString()
    }

    private fun parseJsonToState(jsonStr: String): HoldingGameState {
        val root = JSONObject(jsonStr)
        val holdingName = root.optString("holdingName", "")
        val founderName = root.optString("founderName", "Genç Girişimci")
        val isHoldingNameCustomized = root.optBoolean("isHoldingNameCustomized", holdingName.isNotBlank() && holdingName != "Anadolu Holding")
        val cash = root.optDouble("cash", 50_000.0)

        // Companies
        val companies = mutableListOf<Company>()
        val compArr = root.optJSONArray("companies")
        if (compArr != null) {
            for (i in 0 until compArr.length()) {
                val cObj = compArr.getJSONObject(i)
                val sector = try { Sector.valueOf(cObj.getString("sector")) } catch (_: Exception) { Sector.MARKET }
                val pricing = try { PricingStrategy.valueOf(cObj.optString("pricingStrategy", "BALANCED")) } catch (_: Exception) { PricingStrategy.BALANCED }
                companies.add(
                    Company(
                        id = cObj.getString("id"),
                        name = cObj.getString("name"),
                        sector = sector,
                        level = cObj.optInt("level", 1),
                        staffCount = cObj.optInt("staffCount", sector.baseStaff),
                        pricingStrategy = pricing,
                        marketingDaysLeft = cObj.optInt("marketingDaysLeft", 0),
                        automationLevel = cObj.optInt("automationLevel", 0),
                        totalRevenueEarned = cObj.optDouble("totalRevenueEarned", 0.0),
                        foundedDay = cObj.optInt("foundedDay", 1)
                    )
                )
            }
        }

        // Bank
        val bObj = root.optJSONObject("bank")
        val depositBalance = bObj?.optDouble("depositBalance", 0.0) ?: 0.0
        val creditScore = bObj?.optInt("creditScore", 1150) ?: 1150
        val activeLoans = mutableListOf<ActiveLoan>()
        val loansArr = bObj?.optJSONArray("activeLoans")
        if (loansArr != null) {
            for (i in 0 until loansArr.length()) {
                val lObj = loansArr.getJSONObject(i)
                activeLoans.add(
                    ActiveLoan(
                        id = lObj.getString("id"),
                        name = lObj.getString("name"),
                        principalAmount = lObj.getDouble("principalAmount"),
                        remainingBalance = lObj.getDouble("remainingBalance"),
                        dailyPayment = lObj.getDouble("dailyPayment"),
                        daysRemaining = lObj.getInt("daysRemaining"),
                        interestRateAnnual = lObj.getDouble("interestRateAnnual")
                    )
                )
            }
        }
        val bank = BankData(depositBalance, activeLoans, creditScore)

        // Economy
        val eObj = root.optJSONObject("economy")
        val economy = if (eObj != null) {
            val sentiment = try { MarketSentiment.valueOf(eObj.optString("sentiment", "STABLE")) } catch (_: Exception) { MarketSentiment.STABLE }
            EconomyIndicators(
                day = eObj.optInt("day", 1),
                usdTry = eObj.optDouble("usdTry", 34.50),
                eurTry = eObj.optDouble("eurTry", 37.80),
                goldGramTry = eObj.optDouble("goldGramTry", 3020.0),
                inflationAnnual = eObj.optDouble("inflationAnnual", 44.5),
                policyRate = eObj.optDouble("policyRate", 48.0),
                bist100Index = eObj.optDouble("bist100Index", 9850.0),
                sentiment = sentiment
            )
        } else EconomyIndicators()

        // Stocks
        val stocks = mutableListOf<BistStock>()
        val stArr = root.optJSONArray("stocks")
        if (stArr != null && stArr.length() > 0) {
            for (i in 0 until stArr.length()) {
                val sObj = stArr.getJSONObject(i)
                val hArr = sObj.optJSONArray("priceHistory")
                val hist = mutableListOf<Double>()
                if (hArr != null) {
                    for (h in 0 until hArr.length()) hist.add(hArr.getDouble(h))
                }
                val sec = try {
                    val sStr = sObj.optString("sector", "")
                    if (sStr.isNotBlank()) Sector.valueOf(sStr) else null
                } catch (_: Exception) { null }

                stocks.add(
                    BistStock(
                        ticker = sObj.getString("ticker"),
                        name = sObj.getString("name"),
                        sectorName = sObj.getString("sectorName"),
                        sector = sec,
                        currentPrice = sObj.getDouble("currentPrice"),
                        openPrice = sObj.getDouble("openPrice"),
                        dayHigh = sObj.optDouble("dayHigh", sObj.getDouble("currentPrice")),
                        dayLow = sObj.optDouble("dayLow", sObj.getDouble("currentPrice")),
                        volume = sObj.optLong("volume", 2_500_000L),
                        priceHistory = hist.ifEmpty { listOf(sObj.getDouble("currentPrice")) },
                        liveTicks = listOf(sObj.getDouble("currentPrice")),
                        ownedShares = sObj.optInt("ownedShares", 0),
                        avgPurchasePrice = sObj.optDouble("avgPurchasePrice", 0.0)
                    )
                )
            }
        }

        // Merge with default stocks to ensure full sector stock coverage
        val finalStocks = defaultStocks().map { def ->
            val existing = stocks.find { it.ticker == def.ticker }
            if (existing != null) {
                def.copy(
                    currentPrice = existing.currentPrice,
                    openPrice = existing.openPrice,
                    dayHigh = maxOf(existing.dayHigh, existing.currentPrice),
                    dayLow = minOf(existing.dayLow, existing.currentPrice),
                    priceHistory = existing.priceHistory,
                    ownedShares = existing.ownedShares,
                    avgPurchasePrice = existing.avgPurchasePrice
                )
            } else def
        }

        // Currencies
        val currencies = mutableListOf<CurrencyAsset>()
        val curArr = root.optJSONArray("currencies")
        if (curArr != null && curArr.length() > 0) {
            for (i in 0 until curArr.length()) {
                val curObj = curArr.getJSONObject(i)
                val hArr = curObj.optJSONArray("history")
                val hist = mutableListOf<Double>()
                if (hArr != null) {
                    for (h in 0 until hArr.length()) hist.add(hArr.getDouble(h))
                }
                currencies.add(
                    CurrencyAsset(
                        code = curObj.getString("code"),
                        name = curObj.getString("name"),
                        symbol = curObj.getString("symbol"),
                        currentRate = curObj.getDouble("currentRate"),
                        openRate = curObj.getDouble("openRate"),
                        history = hist.ifEmpty { listOf(curObj.getDouble("currentRate")) },
                        ownedAmount = curObj.optDouble("ownedAmount", 0.0),
                        avgBuyRate = curObj.optDouble("avgBuyRate", 0.0)
                    )
                )
            }
        }

        // Properties
        val properties = mutableListOf<RealEstateProperty>()
        val pArr = root.optJSONArray("properties")
        if (pArr != null && pArr.length() > 0) {
            for (i in 0 until pArr.length()) {
                val pObj = pArr.getJSONObject(i)
                properties.add(
                    RealEstateProperty(
                        id = pObj.getString("id"),
                        name = pObj.getString("name"),
                        location = pObj.getString("location"),
                        purchasePrice = pObj.getDouble("purchasePrice"),
                        dailyRentIncome = pObj.getDouble("dailyRentIncome"),
                        ownedCount = pObj.optInt("ownedCount", 0)
                    )
                )
            }
        }

        // Executives
        val executives = mutableListOf<ExecutiveState>()
        val exArr = root.optJSONArray("executives")
        if (exArr != null && exArr.length() > 0) {
            for (i in 0 until exArr.length()) {
                val exObj = exArr.getJSONObject(i)
                val role = try { ExecutiveRole.valueOf(exObj.getString("role")) } catch (_: Exception) { ExecutiveRole.CFO }
                executives.add(
                    ExecutiveState(
                        role = role,
                        isHired = exObj.optBoolean("isHired", false),
                        hiredDay = exObj.optInt("hiredDay", 0)
                    )
                )
            }
        }

        return HoldingGameState(
            holdingName = holdingName,
            founderName = founderName,
            isHoldingNameCustomized = isHoldingNameCustomized,
            cash = cash,
            companies = companies,
            stocks = finalStocks,
            currencies = if (currencies.isNotEmpty()) currencies else defaultCurrencies(),
            properties = if (properties.isNotEmpty()) properties else defaultProperties(),
            executives = if (executives.isNotEmpty()) executives else defaultExecutives(),
            bank = bank,
            economy = economy
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
