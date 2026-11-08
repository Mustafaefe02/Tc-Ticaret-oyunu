package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExecutiveRole
import com.example.model.ExecutiveState
import com.example.model.HoldingGameState
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun ExecutivesScreen(
    state: HoldingGameState,
    onHireExecutive: (ExecutiveRole) -> Unit,
    onFireExecutive: (ExecutiveRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var roleToFireConfirm by remember { mutableStateOf<ExecutiveRole?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            // Summary Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Holding İnsan Kaynakları & Yönetim Kurulu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "Holdinginizin başına tecrübeli C-Suite liderleri getirerek holding çapında kalıcı çarpan avantajları elde edin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavySurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Toplam İş Gücü", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                            Text(
                                "${state.totalEmployees} Kişi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aktif Yöneticiler", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
                            Text(
                                "${state.executives.count { it.isHired }} / 4",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "C-Suite Üst Düzey Yöneticiler",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        }

        items(state.executives, key = { it.role.name }) { execState ->
            val canAfford = state.cash >= execState.role.hiringCost

            ExecutiveCard(
                execState = execState,
                canAfford = canAfford,
                onHire = { onHireExecutive(execState.role) },
                onFire = { roleToFireConfirm = execState.role }
            )
        }
    }

    roleToFireConfirm?.let { role ->
        AlertDialog(
            onDismissRequest = { roleToFireConfirm = null },
            containerColor = DarkNavyCardElevated,
            title = { Text("${role.title} ile Yolları Ayır", color = TextWhite) },
            text = {
                Text(
                    "Bu yöneticiyi görevden aldığınızda sağladığı tüm kurumsal avantajlar sona erecektir.",
                    color = TextLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onFireExecutive(role)
                        roleToFireConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseLoss)
                ) {
                    Text("Görevden Al", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roleToFireConfirm = null }) {
                    Text("İptal", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ExecutiveCard(
    execState: ExecutiveState,
    canAfford: Boolean,
    onHire: () -> Unit,
    onFire: () -> Unit
) {
    val role = execState.role

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (execState.isHired) DarkNavyCardElevated else DarkNavyCard
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (execState.isHired) GoldPrimary.copy(alpha = 0.5f) else SlateBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Status badge
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (execState.isHired) GoldPrimary else DarkNavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getRoleIcon(role),
                            contentDescription = null,
                            tint = if (execState.isHired) DarkNavyBase else GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = role.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = role.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }

                if (execState.isHired) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldProfit.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldProfit.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Görevde",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = EmeraldProfit,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buff Summary Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (execState.isHired) EmeraldProfit.copy(alpha = 0.12f) else ElectricBlue.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (execState.isHired) EmeraldProfit.copy(alpha = 0.3f) else ElectricBlue.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (execState.isHired) EmeraldProfit else ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = role.buffSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (execState.isHired) EmeraldLight else ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = role.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (execState.isHired) "Günlük Maaş" else "Transfer Ücreti",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                    Text(
                        text = if (execState.isHired)
                            "${Formatters.formatLira(role.dailySalary)} / gün"
                        else
                            Formatters.formatLira(role.hiringCost),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (execState.isHired) RoseLoss else GoldLight
                        )
                    )
                }

                if (execState.isHired) {
                    OutlinedButton(
                        onClick = onFire,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseLoss),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseLoss.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Yolları Ayır", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onHire,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkNavyBase,
                            disabledContainerColor = DarkNavyCardElevated,
                            disabledContentColor = TextSubtle
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("hire_${role.name.lowercase()}")
                    ) {
                        Icon(imageVector = Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transfer Et", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun getRoleIcon(role: ExecutiveRole): ImageVector {
    return when (role) {
        ExecutiveRole.CFO -> Icons.Default.Calculate
        ExecutiveRole.COO -> Icons.Default.Hub
        ExecutiveRole.CMO -> Icons.Default.Campaign
        ExecutiveRole.CTO -> Icons.Default.Memory
    }
}
