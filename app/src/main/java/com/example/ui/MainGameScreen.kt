package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MainTab
import com.example.ui.components.HoldingHeader
import com.example.ui.components.HoldingStatCards
import com.example.ui.components.KeyboardShortcutsDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.HoldingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MainGameScreen(
    viewModel: HoldingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val onlineHoldings by viewModel.onlineHoldings.collectAsStateWithLifecycle()
    val isSyncingOnline by viewModel.isSyncingOnline.collectAsStateWithLifecycle()

    var showShortcutsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // Listen to ViewModel snackbar events
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Auto-focus to receive PC keyboard shortcuts
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.One, Key.NumPad1 -> {
                            viewModel.selectTab(MainTab.COMPANIES)
                            true
                        }
                        Key.Two, Key.NumPad2 -> {
                            viewModel.selectTab(MainTab.FOUND)
                            true
                        }
                        Key.Three, Key.NumPad3 -> {
                            viewModel.selectTab(MainTab.HR)
                            true
                        }
                        Key.Four, Key.NumPad4 -> {
                            viewModel.selectTab(MainTab.MARKETS)
                            true
                        }
                        Key.Five, Key.NumPad5 -> {
                            viewModel.selectTab(MainTab.BANK)
                            true
                        }
                        Key.Six, Key.NumPad6 -> {
                            viewModel.selectTab(MainTab.NEWS)
                            true
                        }
                        Key.R -> {
                            viewModel.syncToMultiplayerLeaderboard()
                            true
                        }
                        Key.S -> {
                            viewModel.toggleSettingsDialog(true)
                            true
                        }
                        Key.K, Key.H, Key.Slash -> {
                            showShortcutsDialog = !showShortcutsDialog
                            true
                        }
                        Key.Escape -> {
                            if (showShortcutsDialog) {
                                showShortcutsDialog = false
                                true
                            } else if (showSettings) {
                                viewModel.toggleSettingsDialog(false)
                                true
                            } else if (viewModel.activeDetailCompanyId.value != null) {
                                viewModel.closeCompanyDetail()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        val isWideScreen = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkNavyBase,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (isWideScreen) 16.dp else 72.dp)
                ) { snackbarData ->
                    Snackbar(
                        containerColor = DarkNavyCardElevated,
                        contentColor = TextWhite,
                        shape = RoundedCornerShape(10.dp),
                        snackbarData = snackbarData
                    )
                }
            },
            bottomBar = {
                // On mobile / compact portrait: standard bottom bar
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = DarkNavySurface,
                        contentColor = TextWhite,
                        tonalElevation = 8.dp
                    ) {
                        MainTab.values().forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = getTabIcon(tab),
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkNavyBase,
                                    selectedTextColor = GoldPrimary,
                                    indicatorColor = GoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Desktop / PC Side Navigation Rail
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = DarkNavySurface,
                        contentColor = TextWhite,
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(GoldPrimary, GoldDark))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BusinessCenter,
                                        contentDescription = "Holding",
                                        tint = DarkNavyBase,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "HOLDING",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = GoldPrimary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 84.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MainTab.values().forEachIndexed { index, tab ->
                                    val selected = selectedTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (selected) DarkNavyBase else DarkNavyCardElevated,
                                                        modifier = Modifier.size(16.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "${index + 1}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = if (selected) GoldPrimary else TextMuted
                                                            )
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = getTabIcon(tab),
                                                    contentDescription = tab.title
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = DarkNavyBase,
                                            selectedTextColor = GoldPrimary,
                                            indicatorColor = GoldPrimary,
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }

                            // Bottom actions on rail (Keyboard Help & Settings)
                            Column(
                                modifier = Modifier.padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { showShortcutsDialog = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DarkNavyCard)
                                        .testTag("rail_shortcuts_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "PC Klavye Kısayolları",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleSettingsDialog(true) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DarkNavyCard)
                                        .testTag("rail_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ayarlar",
                                        tint = TextLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Vertical separator between rail and content
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(SlateBorder)
                    )
                }

                // Main Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Persistent Executive Top App Bar & Economic Ticker
                    HoldingHeader(
                        holdingName = state.holdingName.ifBlank { "HoldingTR" },
                        playerRank = state.playerRank,
                        economy = state.economy,
                        onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                        onOpenShortcuts = { showShortcutsDialog = true }
                    )

                    // Persistent Financial Stat Cards
                    HoldingStatCards(state = state)

                    // Dynamic Content Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { targetTab ->
                            when (targetTab) {
                                MainTab.COMPANIES -> CompaniesScreen(
                                    state = state,
                                    onNavigateToFound = { viewModel.selectTab(MainTab.FOUND) },
                                    onUpgradeCompany = viewModel::upgradeCompany,
                                    onAdjustStaff = { id, delta ->
                                        if (delta > 0) viewModel.hireCompanyStaff(id, delta)
                                        else viewModel.fireCompanyStaff(id, -delta)
                                    },
                                    onSetPricing = viewModel::setCompanyPricing,
                                    onStartMarketing = { id, _ -> viewModel.launchMarketingCampaign(id) },
                                    onUpgradeAutomation = viewModel::upgradeAutomation,
                                    onSellCompany = viewModel::sellCompany
                                )
                                MainTab.FOUND -> FoundCompanyScreen(
                                    state = state,
                                    onFoundCompany = viewModel::foundCompany
                                )
                                MainTab.HR -> ExecutivesScreen(
                                    state = state,
                                    onHireExecutive = viewModel::hireExecutive,
                                    onFireExecutive = viewModel::fireExecutive
                                )
                                MainTab.MARKETS -> MarketsScreen(
                                    state = state,
                                    onBuyStock = viewModel::buyStock,
                                    onSellStock = viewModel::sellStock,
                                    onBuyCurrency = viewModel::buyCurrency,
                                    onSellCurrency = viewModel::sellCurrency,
                                    onBuyProperty = viewModel::buyProperty,
                                    onSellAllStock = viewModel::sellAllStock
                                )
                                MainTab.BANK -> BankScreen(
                                    state = state,
                                    onDeposit = viewModel::depositBank,
                                    onWithdraw = viewModel::withdrawBank,
                                    onTakeLoan = viewModel::takeLoan
                                )
                                MainTab.NEWS -> NewsAndRankingScreen(
                                    state = state,
                                    onlineHoldings = onlineHoldings,
                                    isSyncingOnline = isSyncingOnline,
                                    onSyncOnline = viewModel::syncToMultiplayerLeaderboard
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Onboarding modal: if player hasn't named their holding yet
    if (!state.isHoldingNameCustomized) {
        val currentUser = Firebase.auth.currentUser
        HoldingNamingDialog(
            initialFounderName = currentUser?.displayName ?: state.founderName,
            onConfirm = { holdingName, founderName ->
                viewModel.setupHoldingName(holdingName, founderName)
            }
        )
    }

    if (showShortcutsDialog) {
        KeyboardShortcutsDialog(
            onDismiss = { showShortcutsDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentHoldingName = state.holdingName,
            onDismiss = { viewModel.toggleSettingsDialog(false) },
            onRenameHolding = viewModel::updateHoldingName,
            onResetGame = { viewModel.resetGame() }
        )
    }
}

private fun getTabIcon(tab: MainTab): ImageVector {
    return when (tab) {
        MainTab.COMPANIES -> Icons.Default.CorporateFare
        MainTab.FOUND -> Icons.Default.AddBusiness
        MainTab.HR -> Icons.Default.PeopleAlt
        MainTab.MARKETS -> Icons.Default.TrendingUp
        MainTab.BANK -> Icons.Default.AccountBalance
        MainTab.NEWS -> Icons.Default.Leaderboard
    }
}
