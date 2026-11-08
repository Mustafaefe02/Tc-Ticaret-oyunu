package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    currentHoldingName: String,
    onDismiss: () -> Unit,
    onRenameHolding: (String) -> Unit,
    onResetGame: () -> Unit
) {
    var holdingNameInput by remember { mutableStateOf(currentHoldingName) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Holding Ayarları & Yönetim",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rename Holding
                Text(
                    text = "Holding Unvanı Değiştir",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                OutlinedTextField(
                    value = holdingNameInput,
                    onValueChange = { holdingNameInput = it },
                    label = { Text("Holding Adı") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holding_name_input")
                )

                Button(
                    onClick = { onRenameHolding(holdingNameInput) },
                    enabled = holdingNameInput.isNotBlank() && holdingNameInput != currentHoldingName,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkNavyBase),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adı Kaydet", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = SlateBorder)

                // Game Guide button
                OutlinedButton(
                    onClick = { showGuide = !showGuide },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showGuide) "Rehberi Gizle" else "Nasıl Oynanır? (Rehber)", fontWeight = FontWeight.Bold)
                }

                if (showGuide) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Başlangıç:", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)
                            Text("50.000 TL sermayeyle ilk süpermarketinizi kurun. Günler ilerledikçe günlük kâr kasanıza akar.", color = TextLight, fontSize = 11.sp)

                            Text("2. Büyüme:", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)
                            Text("Kazandığınız parayla şubeleri büyütün, reklam verin, otomasyon yatırımı yapın ve yeni sektörlere girin.", color = TextLight, fontSize = 11.sp)

                            Text("3. Finans & Borsa:", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)
                            Text("Boşta duran nakdinizi vadeli mevduata yatırın, BIST hisseleri ve döviz alıp satarak servetinizi katlayın.", color = TextLight, fontSize = 11.sp)

                            Text("4. C-Suite Yönetim:", fontWeight = FontWeight.Bold, color = GoldLight, fontSize = 12.sp)
                            Text("CFO, COO, CMO ve CTO transfer ederek kalıcı holding çarpanları kazanın ve Forbes Türkiye'de 1 numara olun!", color = TextLight, fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider(color = SlateBorder)

                // Reset Game
                Button(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLoss),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_game_button")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Oyunu Sıfırla (Yeni Oyun)", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = DarkNavySurface,
            title = { Text("Tüm İlerleme Sıfırlansın mı?", color = TextWhite) },
            text = {
                Text(
                    "Mevcut holdinginiz, tüm şirketleriniz ve yatırımlarınız sıfırlanacak ve 50.000 TL başlangıç sermayesiyle yeniden başlayacaksınız.",
                    color = TextLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetGame()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLoss)
                ) {
                    Text("Evet, Sıfırla", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("İptal", color = TextMuted)
                }
            }
        )
    }
}
