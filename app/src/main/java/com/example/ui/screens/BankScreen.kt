package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExecutiveRole
import com.example.model.HoldingGameState
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun BankScreen(
    state: HoldingGameState,
    onDeposit: (Double) -> Unit,
    onWithdraw: (Double) -> Unit,
    onTakeLoan: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDepositModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }
    val hasCfo = state.executives.any { it.role == ExecutiveRole.CFO && it.isHired }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // Section 1: Mevduat Hesabı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkNavyCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Yüksek Getirili Vadeli Mevduat",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Text(
                                    text = "Yıllık Politika Faizi: %${Formatters.formatRate(state.economy.policyRate)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = EmeraldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkNavySurface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mevduat Bakiyesi", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                Formatters.formatLira(state.bank.depositBalance),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                            )
                        }

                        val dailyInterest = state.bank.calculateDailyDepositInterest(state.economy.policyRate)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Günlük Faiz Getirisi", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                "+${Formatters.formatLira(dailyInterest)} / gün",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldProfit)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDepositModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkNavyBase),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("bank_deposit_button")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Para Yatır", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showWithdrawModal = true },
                            enabled = state.bank.depositBalance > 0,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("bank_withdraw_button")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Para Çek", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 2: Kredi Notu & Kredi Merkezi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kurumsal Kredi Notu (Findeks)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = "Kredilerinizi zamanında ödedikçe notunuz yükselir.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                state.bank.creditScore >= 1500 -> EmeraldProfit.copy(alpha = 0.2f)
                                state.bank.creditScore >= 1200 -> GoldPrimary.copy(alpha = 0.2f)
                                else -> RoseLoss.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = "${state.bank.creditScore} / 1900",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        state.bank.creditScore >= 1500 -> EmeraldProfit
                                        state.bank.creditScore >= 1200 -> GoldLight
                                        else -> RoseLoss
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (hasCfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⭐ CFO avantajı: Kredi faiz oranlarında %15 kurumsal indirim devrede!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = GoldLight
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Kullanılabilir Banka Kredileri",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        }

        // Loan Presets
        item {
            LoanOfferCard(
                title = "KOBİ Destek Kredisi",
                principal = 150_000.0,
                days = 60,
                baseInterest = 48.0,
                hasCfo = hasCfo,
                isEligible = true,
                requirementText = "Tüm girişimcilere açık",
                onTake = { onTakeLoan(1) }
            )
        }

        item {
            val eligible = state.netWorth >= 500_000.0
            LoanOfferCard(
                title = "Holding Büyüme Kredisi",
                principal = 1_000_000.0,
                days = 120,
                baseInterest = 44.0,
                hasCfo = hasCfo,
                isEligible = eligible,
                requirementText = "Holding Değeri ₺500.000+",
                onTake = { onTakeLoan(2) }
            )
        }

        item {
            val eligible = state.netWorth >= 5_000_000.0 && state.bank.creditScore >= 1400
            LoanOfferCard(
                title = "Sendikasyon Yatırım Kredisi",
                principal = 10_000_000.0,
                days = 240,
                baseInterest = 40.0,
                hasCfo = hasCfo,
                isEligible = eligible,
                requirementText = "Holding Değeri ₺5 Mn+ & Kredi Notu 1400+",
                onTake = { onTakeLoan(3) }
            )
        }

        // Active Loans
        if (state.bank.activeLoans.isNotEmpty()) {
            item {
                Text(
                    text = "Aktif Kredi Borçları (${state.bank.activeLoans.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            }

            items(state.bank.activeLoans, key = { it.id }) { loan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseLoss.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(loan.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextWhite)
                            Text(
                                "Kalan: ${loan.daysRemaining} Gün",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kalan Borç: ${Formatters.formatLira(loan.remainingBalance)}", style = MaterialTheme.typography.bodySmall, color = RoseLoss)
                            Text("Günlük Taksit: -${Formatters.formatLira(loan.dailyPayment)} / gün", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    if (showDepositModal) {
        DepositWithdrawModal(
            isDeposit = true,
            maxAmount = state.cash,
            onDismiss = { showDepositModal = false },
            onConfirm = {
                onDeposit(it)
                showDepositModal = false
            }
        )
    }

    if (showWithdrawModal) {
        DepositWithdrawModal(
            isDeposit = false,
            maxAmount = state.bank.depositBalance,
            onDismiss = { showWithdrawModal = false },
            onConfirm = {
                onWithdraw(it)
                showWithdrawModal = false
            }
        )
    }
}

@Composable
private fun LoanOfferCard(
    title: String,
    principal: Double,
    days: Int,
    baseInterest: Double,
    hasCfo: Boolean,
    isEligible: Boolean,
    requirementText: String,
    onTake: () -> Unit
) {
    val effectiveRate = if (hasCfo) baseInterest * 0.85 else baseInterest
    val totalRepay = principal * (1.0 + (effectiveRate / 100.0) * (days / 365.0))
    val dailyPayment = totalRepay / days

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEligible) ElectricBlue.copy(alpha = 0.4f) else SlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextWhite)
                    Text("Şart: $requirementText", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
                }
                Text(
                    Formatters.formatLiraCompact(principal),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavySurface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Vade", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("$days Gün", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextWhite))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Yıllık Faiz", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("%${Formatters.formatRate(effectiveRate)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GoldLight))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Günlük Taksit", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text(Formatters.formatLira(dailyPayment), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = RoseLoss))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onTake,
                enabled = isEligible,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = TextWhite,
                    disabledContainerColor = DarkNavyCardElevated,
                    disabledContentColor = TextSubtle
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    text = if (isEligible) "Krediyi Kullan (${Formatters.formatLiraCompact(principal)})" else "Kredi Şartları Karşılanmıyor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DepositWithdrawModal(
    isDeposit: Boolean,
    maxAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("10000") }
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val isValid = amount > 0 && amount <= maxAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        title = {
            Text(
                text = if (isDeposit) "Vadeli Hesaba Para Yatır" else "Vadeli Hesaptan Para Çek",
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Maksimum Kullanılabilir: ${Formatters.formatLira(maxAmount)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Tutar (TL)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.25, 0.50, 1.0).forEach { ratio ->
                        OutlinedButton(
                            onClick = { amountInput = (maxAmount * ratio).toLong().toString() },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (ratio == 1.0) "Tümü" else "%${(ratio * 100).toInt()}", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkNavyBase)
            ) {
                Text(if (isDeposit) "Yatır" else "Çek", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextMuted)
            }
        }
    )
}
