package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Company
import com.example.model.ExecutiveRole
import com.example.model.HoldingGameState
import com.example.model.PricingStrategy
import com.example.model.Sector
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun CompaniesScreen(
    state: HoldingGameState,
    onNavigateToFound: () -> Unit,
    onUpgradeCompany: (String) -> Unit,
    onAdjustStaff: (String, Int) -> Unit,
    onSetPricing: (String, PricingStrategy) -> Unit,
    onStartMarketing: (String, Int) -> Unit,
    onUpgradeAutomation: (String) -> Unit,
    onSellCompany: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCompanyForDetail by remember { mutableStateOf<Company?>(null) }

    // If detail modal is open, ensure company state is refreshed
    val activeCompany = selectedCompanyForDetail?.let { current ->
        state.companies.find { it.id == current.id }
    }

    if (state.companies.isEmpty()) {
        EmptyCompaniesView(
            cash = state.cash,
            onFoundClick = onNavigateToFound,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Holding Şirketleri (${state.companies.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    TextButton(
                        onClick = onNavigateToFound,
                        colors = ButtonDefaults.textButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yeni Şirket Kur", fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(state.companies, key = { it.id }) { company ->
                val hasCfo = state.executives.any { it.role == ExecutiveRole.CFO && it.isHired }
                val hasCoo = state.executives.any { it.role == ExecutiveRole.COO && it.isHired }
                val hasCmo = state.executives.any { it.role == ExecutiveRole.CMO && it.isHired }
                val eventMultiplier = state.activeNewsEffects
                    .filter { it.affectedSector == null || it.affectedSector == company.sector }
                    .fold(1.0) { acc, event -> acc * event.impactMultiplier }

                val fin = company.calculateDailyFinancials(hasCfo, hasCoo, hasCmo, eventMultiplier)

                CompanyListItemCard(
                    company = company,
                    netDailyProfit = fin.netProfit,
                    grossRevenue = fin.grossRevenue,
                    onManageClick = { selectedCompanyForDetail = company }
                )
            }
        }
    }

    if (activeCompany != null) {
        CompanyDetailBottomSheet(
            company = activeCompany,
            cash = state.cash,
            hasCto = state.executives.any { it.role == ExecutiveRole.CTO && it.isHired },
            hasCmo = state.executives.any { it.role == ExecutiveRole.CMO && it.isHired },
            onDismiss = { selectedCompanyForDetail = null },
            onUpgrade = { onUpgradeCompany(activeCompany.id) },
            onAdjustStaff = { delta -> onAdjustStaff(activeCompany.id, delta) },
            onSetPricing = { strat -> onSetPricing(activeCompany.id, strat) },
            onStartMarketing = { type -> onStartMarketing(activeCompany.id, type) },
            onUpgradeAutomation = { onUpgradeAutomation(activeCompany.id) },
            onSell = {
                onSellCompany(activeCompany.id)
                selectedCompanyForDetail = null
            }
        )
    }
}

@Composable
fun EmptyCompaniesView(
    cash: Double,
    onFoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DarkNavyCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Henüz Bir Şirketiniz Yok",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cebinizdeki ₺50.000 başlangıç sermayesiyle ilk süpermarketinizi kurun ve Türkiye'nin en büyük holdingi olma yolculuğuna başlayın!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFoundClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = DarkNavyBase
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("found_first_company_button")
        ) {
            Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "İlk İşletmeni Kur (50.000 TL)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun CompanyListItemCard(
    company: Company,
    netDailyProfit: Double,
    grossRevenue: Double,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onManageClick)
            .testTag("company_card_${company.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Sector Icon, Name, Level badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkNavyCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSectorIcon(company.sector),
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = company.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite,
                            maxLines = 1
                        )
                        Text(
                            text = company.sector.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricBlue.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Sv. ${company.level} Şube",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = ElectricBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Marketing, Automation, Pricing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (company.marketingDaysLeft > 0) {
                    MiniBadge(
                        text = "📢 Reklam: ${company.marketingDaysLeft} gün",
                        color = GoldLight,
                        bgColor = GoldPrimary.copy(alpha = 0.15f)
                    )
                }
                if (company.automationLevel > 0) {
                    MiniBadge(
                        text = "⚙️ Otomasyon Sv. ${company.automationLevel}",
                        color = CyanAccent,
                        bgColor = CyanAccent.copy(alpha = 0.15f)
                    )
                }
                MiniBadge(
                    text = company.pricingStrategy.title,
                    color = TextLight,
                    bgColor = DarkNavyCardElevated
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Metrics and Manage Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Günlük Net Kâr",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                    Text(
                        text = "${if (netDailyProfit >= 0) "+" else ""}${Formatters.formatLira(netDailyProfit)} / gün",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (netDailyProfit >= 0) EmeraldProfit else RoseLoss
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${company.staffCount}", style = MaterialTheme.typography.bodySmall, color = TextLight)
                    }

                    Button(
                        onClick = onManageClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavyCardElevated,
                            contentColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = "Yönet", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyDetailBottomSheet(
    company: Company,
    cash: Double,
    hasCto: Boolean,
    hasCmo: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    onAdjustStaff: (Int) -> Unit,
    onSetPricing: (PricingStrategy) -> Unit,
    onStartMarketing: (Int) -> Unit,
    onUpgradeAutomation: () -> Unit,
    onSell: () -> Unit
) {
    var showSellConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavySurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = company.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "${company.sector.title} • Sv. ${company.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = TextLight)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Valuation Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Şirket Piyasa Değeri", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                Formatters.formatLira(company.valuation),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldLight
                            )
                        }
                        OutlinedButton(
                            onClick = { showSellConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseLoss),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseLoss.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Şirketi Sat", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // 1. Upgrade Branch
                ActionItemCard(
                    title = "Şube Aç & Kapasite Büyüt",
                    description = "Satış kapasitesini ve günlük ciroyu %85 artırır.",
                    costLabel = Formatters.formatLira(company.upgradeCost),
                    canAfford = cash >= company.upgradeCost,
                    buttonText = "Yükselt (Sv. ${company.level + 1})",
                    onAction = onUpgrade
                )

                // 2. Staff Adjustment
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Personel Yönetimi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Mevcut: ${company.staffCount} çalışan (Önerilen: ${company.sector.baseStaff * company.level})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onAdjustStaff(-1) },
                                enabled = company.staffCount > 1,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("-1 Çalışan Çıkar", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onAdjustStaff(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1 Çalışan Al", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. Pricing Strategy
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Fiyatlandırma Stratejisi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PricingStrategy.values().forEach { strategy ->
                                val selected = company.pricingStrategy == strategy
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GoldPrimary else DarkNavyCardElevated)
                                        .clickable { onSetPricing(strategy) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strategy.title.split(" ").first(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (selected) DarkNavyBase else TextLight
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Marketing Campaign
                val cmoDurationBonus = if (hasCmo) " (+%50 CMO Bonusu)" else ""
                ActionItemCard(
                    title = "Dijital Reklam Kampanyası",
                    description = "15 gün boyunca ciroya %25 doğrudan katkı sağlar$cmoDurationBonus.",
                    costLabel = "₺35.000",
                    canAfford = cash >= 35_000.0,
                    buttonText = "Başlat (15 Gün)",
                    onAction = { onStartMarketing(1) }
                )

                // 5. Automation Upgrade
                val ctoDiscount = if (hasCto) " (%50 CTO İndirimli)" else ""
                val autoCost = company.automationUpgradeCost * (if (hasCto) 0.50 else 1.0)
                ActionItemCard(
                    title = "Yapay Zeka & Otomasyon",
                    description = "İşletme maliyetlerini kalıcı olarak %8 düşürür$ctoDiscount.",
                    costLabel = Formatters.formatLira(autoCost),
                    canAfford = cash >= autoCost,
                    buttonText = "Geliştir (Sv. ${company.automationLevel + 1})",
                    onAction = onUpgradeAutomation
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showSellConfirm) {
        val saleProceeds = company.valuation * 0.90
        AlertDialog(
            onDismissRequest = { showSellConfirm = false },
            containerColor = DarkNavyCardElevated,
            title = { Text("Şirketi Satmak İstiyor Musunuz?", color = TextWhite) },
            text = {
                Text(
                    "Bu işlem ${company.name} şirketini mülkiyetinizden çıkaracak ve kasaya ₺${Formatters.formatLira(saleProceeds)} nakit aktaracaktır.",
                    color = TextLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSellConfirm = false
                        onSell()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLoss)
                ) {
                    Text("Evet, Sat", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellConfirm = false }) {
                    Text("İptal", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ActionItemCard(
    title: String,
    description: String,
    costLabel: String,
    canAfford: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maliyet: $costLabel",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (canAfford) GoldPrimary else RoseLoss
                )
                Button(
                    onClick = onAction,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkNavyBase,
                        disabledContainerColor = DarkNavyCardElevated,
                        disabledContentColor = TextSubtle
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MiniBadge(text: String, color: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun getSectorIcon(sector: Sector): ImageVector {
    return when (sector) {
        Sector.MARKET -> Icons.Default.Storefront
        Sector.RESTAURANT -> Icons.Default.Restaurant
        Sector.LOGISTICS -> Icons.Default.LocalShipping
        Sector.MANUFACTURING -> Icons.Default.PrecisionManufacturing
        Sector.CONSTRUCTION -> Icons.Default.Apartment
        Sector.TECHNOLOGY -> Icons.Default.Devices
        Sector.TOURISM -> Icons.Default.BeachAccess
        Sector.FINANCE -> Icons.Default.AccountBalance
    }
}
