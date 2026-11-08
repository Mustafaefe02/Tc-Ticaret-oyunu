package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HoldingRepository
import com.example.data.MultiplayerRepository
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HoldingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HoldingRepository(application.applicationContext)
    private val multiplayerRepository = MultiplayerRepository(application.applicationContext)

    val gameState: StateFlow<HoldingGameState> = repository.gameState

    private val _selectedTab = MutableStateFlow(MainTab.COMPANIES)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Selected company for details modal
    private val _activeDetailCompanyId = MutableStateFlow<String?>(null)
    val activeDetailCompanyId: StateFlow<String?> = _activeDetailCompanyId.asStateFlow()

    // Dialog state
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    // Online players leaderboard
    private val _onlineHoldings = MutableStateFlow<List<OnlineHoldingEntry>>(emptyList())
    val onlineHoldings: StateFlow<List<OnlineHoldingEntry>> = _onlineHoldings.asStateFlow()

    private val _isSyncingOnline = MutableStateFlow(false)
    val isSyncingOnline: StateFlow<Boolean> = _isSyncingOnline.asStateFlow()

    private var realTimeSimulationJob: Job? = null
    private var liveTickerJob: Job? = null
    private var multiplayerSyncJob: Job? = null

    init {
        startRealTimeSimulationLoop()
        startLiveMarketTicker()
        observeMultiplayerLeaderboard()
    }

    /**
     * Real-time automated continuous business progression:
     * Advances financial quarter/day every 3.5 seconds in background smoothly.
     * No manual button or 1x/2x speed controls required.
     */
    private fun startRealTimeSimulationLoop() {
        realTimeSimulationJob?.cancel()
        realTimeSimulationJob = viewModelScope.launch {
            while (isActive) {
                delay(3500L) // 3.5s per business cycle
                repository.advanceDay()

                // Every 3 cycles sync to cloud multiplayer leaderboard
                if (repository.gameState.value.isHoldingNameCustomized && Firebase.auth.currentUser != null) {
                    syncToMultiplayerLeaderboard()
                }
            }
        }
    }

    private fun startLiveMarketTicker() {
        liveTickerJob?.cancel()
        liveTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1600L) // Real-time market ticks every 1.6s
                repository.tickLiveMarketPrices()
            }
        }
    }

    private fun observeMultiplayerLeaderboard() {
        multiplayerSyncJob?.cancel()
        multiplayerSyncJob = viewModelScope.launch {
            multiplayerRepository.observeLeaderboard().collect { list ->
                _onlineHoldings.value = list
            }
        }
    }

    fun syncToMultiplayerLeaderboard() {
        val state = gameState.value
        if (!state.isHoldingNameCustomized || state.holdingName.isBlank()) return
        val user = Firebase.auth.currentUser ?: return

        viewModelScope.launch {
            _isSyncingOnline.value = true
            try {
                multiplayerRepository.publishHoldingStats(
                    holdingName = state.holdingName,
                    ownerName = user.displayName?.ifBlank { state.founderName } ?: state.founderName,
                    netWorth = state.netWorth,
                    cash = state.cash,
                    companyCount = state.companies.size,
                    totalEmployees = state.totalEmployees,
                    prestige = state.currentPrestige
                )
            } finally {
                _isSyncingOnline.value = false
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun openCompanyDetail(companyId: String) {
        _activeDetailCompanyId.value = companyId
    }

    fun closeCompanyDetail() {
        _activeDetailCompanyId.value = null
    }

    fun toggleSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    // Actions
    fun foundCompany(name: String, sector: Sector) {
        val success = repository.foundCompany(name, sector)
        if (success) {
            emitMessage("🎉 ${name.ifBlank { sector.title }} şirketi başarıyla kuruldu!")
            _selectedTab.value = MainTab.COMPANIES
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yetersiz sermaye! ${sector.title} için en az ₺${sector.startingCost.toLong()} gerekli.")
        }
    }

    fun upgradeCompany(companyId: String) {
        val success = repository.upgradeCompanyLevel(companyId)
        if (success) {
            emitMessage("🚀 Şirket seviyesi yükseltildi!")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yükseltme için bakiye yetersiz!")
        }
    }

    fun hireCompanyStaff(companyId: String, count: Int = 1) {
        val success = repository.adjustCompanyStaff(companyId, count)
        if (success) {
            emitMessage("👥 $count yeni personel işe alındı.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ İşe alım maliyeti için bakiye yetersiz.")
        }
    }

    fun fireCompanyStaff(companyId: String, count: Int = 1) {
        repository.adjustCompanyStaff(companyId, -count)
        emitMessage("📉 $count çalışan işten çıkarıldı.")
        syncToMultiplayerLeaderboard()
    }

    fun setCompanyPricing(companyId: String, strategy: PricingStrategy) {
        repository.setCompanyPricingStrategy(companyId, strategy)
        emitMessage("🏷️ Fiyatlama stratejisi güncellendi: ${strategy.title}")
    }

    fun launchMarketingCampaign(companyId: String) {
        val success = repository.startMarketingCampaign(companyId, 1)
        if (success) {
            emitMessage("📢 Reklam kampanyası başlatıldı (14 gün +%30 ciro)!")
        } else {
            emitMessage("❌ Reklam bütçesi için en az ₺15.000 gerekli.")
        }
    }

    fun upgradeAutomation(companyId: String) {
        val success = repository.upgradeAutomation(companyId)
        if (success) {
            emitMessage("⚙️ Otomasyon ve ERP sistemi geliştirildi!")
        } else {
            emitMessage("❌ Otomasyon yatırımı için bakiye yetersiz.")
        }
    }

    fun sellCompany(companyId: String) {
        val soldName = repository.sellCompany(companyId)
        closeCompanyDetail()
        if (soldName != null) {
            emitMessage("💰 $soldName satıldı ve holding kasasına aktarıldı.")
            syncToMultiplayerLeaderboard()
        }
    }

    // Trading Actions
    fun buyStock(ticker: String, shares: Int) {
        val success = repository.buyStock(ticker, shares)
        if (success) {
            emitMessage("📈 $shares adet $ticker hissesi portföye eklendi.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Alım için nakit bakiye yetersiz!")
        }
    }

    fun sellStock(ticker: String, shares: Int) {
        val success = repository.sellStock(ticker, shares)
        if (success) {
            emitMessage("📉 $shares adet $ticker hissesi satıldı.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Portföyde yeterli hisse adedi yok!")
        }
    }

    fun sellAllStock(ticker: String) {
        val stock = gameState.value.stocks.find { it.ticker == ticker } ?: return
        if (stock.ownedShares > 0) {
            sellStock(ticker, stock.ownedShares)
        }
    }

    fun buyCurrency(code: String, amountTry: Double) {
        val success = repository.buyCurrency(code, amountTry)
        if (success) {
            emitMessage("💱 Döviz/Altın alımı tamamlandı.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yetersiz nakit bakiye!")
        }
    }

    fun sellCurrency(code: String, amountUnits: Double) {
        val success = repository.sellCurrency(code, amountUnits)
        if (success) {
            emitMessage("💱 Döviz/Altın bozduruldu.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yetersiz varlık miktarı!")
        }
    }

    fun buyProperty(propertyId: String) {
        val success = repository.buyProperty(propertyId)
        if (success) {
            emitMessage("🏢 Gayrimenkul portföyünüze eklendi!")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yetersiz nakit bakiye!")
        }
    }

    fun depositBank(amount: Double) {
        val success = repository.depositToBank(amount)
        if (success) {
            emitMessage("🏦 Vadeli mevduata ₺${amount.toLong()} yatırıldı.")
        } else {
            emitMessage("❌ Yetersiz nakit bakiye!")
        }
    }

    fun withdrawBank(amount: Double) {
        val success = repository.withdrawFromBank(amount)
        if (success) {
            emitMessage("🏦 Vadeli mevduattan ₺${amount.toLong()} çekildi.")
        } else {
            emitMessage("❌ Vadeli hesapta yeterli bakiye bulunmuyor.")
        }
    }

    fun takeLoan(loanType: Int) {
        val success = repository.takeLoan(loanType)
        if (success) {
            emitMessage("✅ Kredi onaylandı ve holding kasasına aktarıldı!")
        } else {
            emitMessage("❌ Kredi başvurusu reddedildi! Holding net değeri veya kredi notu yetersiz.")
        }
    }

    fun hireExecutive(role: ExecutiveRole) {
        val success = repository.hireExecutive(role)
        if (success) {
            emitMessage("🤝 ${role.title} göreve başladı! Holding avantajları devrede.")
            syncToMultiplayerLeaderboard()
        } else {
            emitMessage("❌ Yönetici transferi için nakit yetersiz.")
        }
    }

    fun fireExecutive(role: ExecutiveRole) {
        repository.fireExecutive(role)
        emitMessage("👋 ${role.title} ile yollar ayrıldı.")
        syncToMultiplayerLeaderboard()
    }

    fun setupHoldingName(holdingName: String, founderName: String) {
        repository.updateHoldingName(holdingName, founderName)
        syncToMultiplayerLeaderboard()
        emitMessage("🏛️ ${holdingName.trim()} resmi olarak kuruldu! 50.000 TL ile yarıştasınız.")
    }

    fun updateHoldingName(name: String) {
        repository.updateHoldingName(name)
        syncToMultiplayerLeaderboard()
        emitMessage("🏷️ Holding unvanı güncellendi: $name")
    }

    fun resetGame(name: String? = null) {
        repository.resetGame(name)
        closeCompanyDetail()
        _selectedTab.value = MainTab.COMPANIES
        syncToMultiplayerLeaderboard()
        emitMessage("🔄 Yeni oyun başlatıldı! 50.000 TL ile yolculuğunuz başlıyor.")
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        realTimeSimulationJob?.cancel()
        liveTickerJob?.cancel()
        multiplayerSyncJob?.cancel()
    }
}
