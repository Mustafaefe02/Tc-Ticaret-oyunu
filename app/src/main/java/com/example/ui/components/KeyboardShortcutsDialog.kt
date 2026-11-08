package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun KeyboardShortcutsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCardElevated,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PC & Klavye Kısayolları",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Masaüstü ve klavye ile hızlı yönetim",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CANLI EKONOMİ & MULTIPLAYER",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = GoldLight
                )
                ShortcutItem(keyBadge = "Canlı", action = "Sürekli akan gerçek zamanlı piyasa & çok oyunculu senkronizasyon")
                ShortcutItem(keyBadge = "R", action = "Çok Oyunculu Ligi Yenile / Senkronize Et")

                HorizontalDivider(color = SlateBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "MENÜ VE SEKMELER ARASI GEÇİŞ",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = ElectricBlue
                )
                ShortcutItem(keyBadge = "1", action = "🏢 Şirketlerim")
                ShortcutItem(keyBadge = "2", action = "🛒 İşletme Kur")
                ShortcutItem(keyBadge = "3", action = "👥 İK & Yönetim Kurulu")
                ShortcutItem(keyBadge = "4", action = "📈 Borsa & Varlık Piyasaları")
                ShortcutItem(keyBadge = "5", action = "🏦 Banka & Mevduat")
                ShortcutItem(keyBadge = "6", action = "📰 Bülten & Forbes Sıralaması")

                HorizontalDivider(color = SlateBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "DİĞER KISAYOLLAR",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = EmeraldLight
                )
                ShortcutItem(keyBadge = "S", action = "Holding Ayarları / İsim Değiştir")
                ShortcutItem(keyBadge = "K / H", action = "Bu Kısayol Rehberini Aç")
                ShortcutItem(keyBadge = "Esc", action = "Açık Pencereleri ve Modalları Kapat")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkNavyBase),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("close_shortcuts_dialog")
            ) {
                Text("Anladım", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ShortcutItem(
    keyBadge: String,
    action: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder.copy(alpha = 0.8f))
        ) {
            Text(
                text = keyBadge,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = GoldPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = action,
            style = MaterialTheme.typography.bodySmall,
            color = TextLight,
            modifier = Modifier.weight(1f)
        )
    }
}
