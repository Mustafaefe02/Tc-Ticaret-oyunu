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
import com.example.model.HoldingGameState
import com.example.model.Sector
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun FoundCompanyScreen(
    state: HoldingGameState,
    onFoundCompany: (String, Sector) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSectorToFound by remember { mutableStateOf<Sector?>(null) }
    var customCompanyName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Yeni İşletme & Şirket Kur",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
                Text(
                    text = "8 stratejik sektör arasından seçim yapın. Sermayeniz yettiğinde yeni pazarlara açılın.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        items(Sector.values(), key = { it.id }) { sector ->
            val canAfford = state.cash >= sector.startingCost
            val estimatedDailyNet = sector.calculateDailyNetProfit(1)

            SectorCatalogCard(
                sector = sector,
                canAfford = canAfford,
                estimatedDailyNet = estimatedDailyNet,
                onFoundClick = {
                    selectedSectorToFound = sector
                    customCompanyName = "${state.holdingName} ${sector.title.split(" ").first()}"
                }
            )
        }
    }

    selectedSectorToFound?.let { sector ->
        AlertDialog(
            onDismissRequest = { selectedSectorToFound = null },
            containerColor = DarkNavyCardElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = getSectorIcon(sector), contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${sector.title} Kur",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kuruluş Bedeli: ${Formatters.formatLira(sector.startingCost)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldLight
                    )
                    Text(
                        text = "Şirketinizin ticaret unvanını belirleyin:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = customCompanyName,
                        onValueChange = { customCompanyName = it },
                        label = { Text("Şirket Adı") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("company_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = customCompanyName.ifBlank { "${state.holdingName} ${sector.title}" }
                        onFoundCompany(finalName, sector)
                        selectedSectorToFound = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkNavyBase),
                    modifier = Modifier.testTag("confirm_found_company_button")
                ) {
                    Text("Kur ve Başlat", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSectorToFound = null }) {
                    Text("İptal", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SectorCatalogCard(
    sector: Sector,
    canAfford: Boolean,
    estimatedDailyNet: Double,
    onFoundClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (canAfford) GoldPrimary.copy(alpha = 0.4f) else SlateBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (canAfford) GoldPrimary.copy(alpha = 0.2f) else DarkNavyCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSectorIcon(sector),
                            contentDescription = null,
                            tint = if (canAfford) GoldPrimary else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = sector.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = sector.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats Row: Capital requirement, estimated daily net, initial staff
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavySurface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gereken Sermaye", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text(
                        Formatters.formatLiraCompact(sector.startingCost),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) GoldLight else RoseLoss
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tahmini Net Kâr", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text(
                        "+${Formatters.formatLiraCompact(estimatedDailyNet)} / gün",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldProfit)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Başlangıç Ekibi", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text(
                        "${sector.baseStaff} Kişi",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onFoundClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkNavyBase,
                    disabledContainerColor = DarkNavyCardElevated,
                    disabledContentColor = TextSubtle
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("found_button_${sector.id}")
            ) {
                if (canAfford) {
                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Şirketi Kur (${Formatters.formatLira(sector.startingCost)})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = "Yetersiz Sermaye (${Formatters.formatLira(sector.startingCost)})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}
