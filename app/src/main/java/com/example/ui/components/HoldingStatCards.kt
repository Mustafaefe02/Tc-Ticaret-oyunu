package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.DailySummaryFinancials
import com.example.model.HoldingGameState
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun HoldingStatCards(
    state: HoldingGameState,
    modifier: Modifier = Modifier
) {
    var showBreakdownDialog by remember { mutableStateOf(false) }
    val summary = remember(state) { state.calculateDailySummary() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Primary Highlight Card: Cash & Net Worth
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cash Box
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kasadaki Nakit",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatLira(state.cash),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.cash >= 0) GoldLight else RoseLoss
                            ),
                            maxLines = 1
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(SlateBorder)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Holding Net Worth
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Holding Değeri",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatLiraCompact(state.netWorth),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            ),
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = SlateBorder.copy(alpha = 0.5f)
                )

                // Secondary row: Daily Profit & Capacity Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Daily Net Profit Clickable
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavyCardElevated)
                            .clickable { showBreakdownDialog = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("profit_breakdown_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isProfit = summary.netDailyProfit >= 0
                        Icon(
                            imageVector = if (isProfit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isProfit) EmeraldProfit else RoseLoss,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Günlük Kâr / Zarar",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                            Text(
                                text = "${if (isProfit) "+" else ""}${Formatters.formatLira(summary.netDailyProfit)} / gün",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (isProfit) EmeraldProfit else RoseLoss
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Detay",
                            tint = TextSubtle,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Total staff and companies count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniStat(
                            icon = Icons.Default.CorporateFare,
                            label = "Şirket",
                            value = "${state.companies.size}"
                        )
                        MiniStat(
                            icon = Icons.Default.Group,
                            label = "Çalışan",
                            value = "${state.totalEmployees}"
                        )
                    }
                }
            }
        }
    }

    if (showBreakdownDialog) {
        DailyFinancialsBreakdownDialog(
            summary = summary,
            onDismiss = { showBreakdownDialog = false }
        )
    }
}

@Composable
private fun MiniStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkNavyCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        }
    }
}

@Composable
fun DailyFinancialsBreakdownDialog(
    summary: DailySummaryFinancials,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = GoldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Günlük Finansal Tablo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GELİRLER",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = EmeraldLight
                )
                FinancialRow("Şirket Satış Gelirleri", "+${Formatters.formatLira(summary.grossCompanyRevenue)}", EmeraldProfit)
                if (summary.bankDepositInterest > 0) {
                    FinancialRow("Banka Mevduat Faizi", "+${Formatters.formatLira(summary.bankDepositInterest)}", EmeraldProfit)
                }
                if (summary.realEstateRents > 0) {
                    FinancialRow("Gayrimenkul Kira Gelirleri", "+${Formatters.formatLira(summary.realEstateRents)}", EmeraldProfit)
                }

                HorizontalDivider(color = SlateBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "GİDERLER",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = RoseLight
                )
                FinancialRow("Malzeme & İşletme Maliyeti", "-${Formatters.formatLira(summary.companyOperatingCosts)}", RoseLoss)
                FinancialRow("Çalışan Maaşları", "-${Formatters.formatLira(summary.companyWages)}", RoseLoss)
                FinancialRow("Vergiler", "-${Formatters.formatLira(summary.companyTaxes)}", RoseLoss)
                if (summary.executiveSalaries > 0) {
                    FinancialRow("Yönetici (C-Suite) Maaşları", "-${Formatters.formatLira(summary.executiveSalaries)}", RoseLoss)
                }
                if (summary.bankLoanPayments > 0) {
                    FinancialRow("Banka Kredi Taksitleri", "-${Formatters.formatLira(summary.bankLoanPayments)}", RoseLoss)
                }

                HorizontalDivider(color = SlateBorder, modifier = Modifier.padding(vertical = 4.dp))

                val isProfit = summary.netDailyProfit >= 0
                FinancialRow(
                    label = "GÜNLÜK NET KÂR",
                    amount = "${if (isProfit) "+" else ""}${Formatters.formatLira(summary.netDailyProfit)}",
                    color = if (isProfit) EmeraldProfit else RoseLoss,
                    isBold = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_breakdown_dialog")
            ) {
                Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun FinancialRow(
    label: String,
    amount: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = if (isBold) TextWhite else TextLight
        )
        Text(
            text = amount,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
