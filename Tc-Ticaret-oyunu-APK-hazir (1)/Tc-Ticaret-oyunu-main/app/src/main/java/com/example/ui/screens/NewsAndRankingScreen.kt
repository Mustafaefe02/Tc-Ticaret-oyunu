package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.model.CompetitorHolding
import com.example.model.HoldingGameState
import com.example.model.NewsEvent
import com.example.model.OnlineHoldingEntry
import com.example.ui.theme.*
import com.example.util.Formatters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun NewsAndRankingScreen(
    state: HoldingGameState,
    onlineHoldings: List<OnlineHoldingEntry>,
    isSyncingOnline: Boolean,
    onSyncOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf(0) } // 0: Canlı Oyuncu Ligi, 1: Forbes TR Zenginler, 2: Ekonomi Haberleri, 3: Başarımlar

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sub-tabs navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkNavyCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubTabItem(
                title = "Canlı Oyuncu Ligi",
                selected = subTab == 0,
                onClick = { subTab = 0 },
                modifier = Modifier.weight(1.1f),
                badge = "${onlineHoldings.size}"
            )
            SubTabItem(
                title = "Forbes TR",
                selected = subTab == 1,
                onClick = { subTab = 1 },
                modifier = Modifier.weight(0.9f)
            )
            SubTabItem(
                title = "Haberler",
                selected = subTab == 2,
                onClick = { subTab = 2 },
                modifier = Modifier.weight(0.9f)
            )
            SubTabItem(
                title = "Başarımlar",
                selected = subTab == 3,
                onClick = { subTab = 3 },
                modifier = Modifier.weight(0.9f)
            )
        }

        when (subTab) {
            0 -> OnlineMultiplayerLeaderboardTab(
                state = state,
                onlineHoldings = onlineHoldings,
                isSyncingOnline = isSyncingOnline,
                onSyncOnline = onSyncOnline
            )
            1 -> ForbesLeaderboardTab(state = state)
            2 -> NewsFeedTab(newsHistory = state.newsHistory)
            3 -> AchievementsTab(achievements = state.achievements)
        }
    }
}

@Composable
private fun SubTabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) GoldPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (selected) DarkNavyBase else TextLight,
                maxLines = 1
            )
            if (badge != null && badge != "0") {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) DarkNavyBase else GoldPrimary,
                    modifier = Modifier.size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (selected) GoldPrimary else DarkNavyBase
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 0: ONLINE MULTIPLAYER LEADERBOARD ---
@Composable
private fun OnlineMultiplayerLeaderboardTab(
    state: HoldingGameState,
    onlineHoldings: List<OnlineHoldingEntry>,
    isSyncingOnline: Boolean,
    onSyncOnline: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val currentUserId = currentUser?.uid

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        // Sign-In Card if unauthenticated
        if (currentUser == null) {
            item {
                GoogleSignInCard(
                    onAuthSuccess = onSyncOnline,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // Multiplayer Status & Sync Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = DarkNavyBase,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gerçek Oyuncu Holding Sıralaması",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Text(
                                    text = if (currentUser != null)
                                        "Hesap: ${currentUser.displayName ?: currentUser.email ?: "Giriş yapıldı"}"
                                    else
                                        "Çok oyunculu kapışma modu aktif",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GoldLight
                                )
                            }
                        }

                        if (currentUser != null) {
                            IconButton(
                                onClick = onSyncOnline,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkNavyCard)
                                    .testTag("sync_online_button")
                            ) {
                                if (isSyncingOnline) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = GoldPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Senkronize Et",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkNavySurface
                        ) {
                            Text(
                                text = "Holding: ${state.holdingName.ifBlank { "Tescilsiz" }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkNavySurface
                        ) {
                            Text(
                                text = "Servet: ${Formatters.formatLira(state.netWorth)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Leaderboard List
        if (onlineHoldings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Liderlik tablosu yükleniyor veya henüz ilk oyuncusunuz!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Holdingini zirveye taşımak için şirket kur ve ticaret yap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(onlineHoldings.withIndex().toList(), key = { it.value.id.ifBlank { it.value.userId } }) { (index, item) ->
                val rank = index + 1
                val isMe = currentUserId != null && item.userId == currentUserId

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) DarkNavyCardElevated else DarkNavyCard
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMe) GoldPrimary else SlateBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (rank) {
                                    1 -> GoldPrimary
                                    2 -> Color(0xFFC0C0C0)
                                    3 -> Color(0xFFCD7F32)
                                    else -> DarkNavySurface
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$rank",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (rank in 1..3) DarkNavyBase else TextLight
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.holdingName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isMe) GoldLight else TextWhite
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = GoldPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "SEN",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = GoldPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Kurucu: ${item.ownerName} • ${item.companyCount} Şirket • ${item.totalEmployees} Personel",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatLiraCompact(item.netWorth),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) GoldLight else EmeraldGreen
                                )
                            )
                            Text(
                                text = "Nakit: ${Formatters.formatLiraCompact(item.cash)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 1: FORBES RANKINGS ---
@Composable
private fun ForbesLeaderboardTab(state: HoldingGameState) {
    val competitors = state.topCompetitors

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = DarkNavyBase)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Holding Sıralamanız: #${state.playerRank}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Holding Değeriniz: ${Formatters.formatLira(state.netWorth)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GoldLight
                        )
                    }
                }
            }
        }

        items(competitors.withIndex().toList(), key = { it.value.id }) { (index, item) ->
            val rank = index + 1
            val isPlayer = item.isPlayer

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPlayer) DarkNavyCardElevated else DarkNavyCard
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPlayer) GoldPrimary else SlateBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (rank) {
                                1 -> GoldPrimary
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> DarkNavySurface
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$rank",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (rank in 1..3) DarkNavyBase else TextLight
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isPlayer) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isPlayer) GoldLight else TextWhite
                                )
                                if (isPlayer) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GoldPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "SEN",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            color = GoldPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.founder} • ${item.primarySector}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = Formatters.formatLiraCompact(item.netWorth),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPlayer) GoldLight else TextLight
                        )
                    )
                }
            }
        }
    }
}

// --- SUB-TAB 2: NEWS FEED ---
@Composable
private fun NewsFeedTab(newsHistory: List<NewsEvent>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        items(newsHistory, key = { it.id }) { news ->
            val categoryColor = when (news.category) {
                "Fırsat" -> EmeraldProfit
                "Kriz" -> RoseLoss
                "TCMB" -> GoldPrimary
                else -> ElectricBlue
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = news.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "Gün ${news.day}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = news.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// --- SUB-TAB 3: ACHIEVEMENTS ---
@Composable
private fun AchievementsTab(achievements: List<Achievement>) {
    val unlockedCount = achievements.count { it.isUnlocked }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kazanılan Başarımlar: $unlockedCount / ${achievements.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "Her başarım holdinginize prestij ve piyasa güveni kazandırır.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }
            }
        }

        items(achievements, key = { it.id }) { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) DarkNavyCardElevated else DarkNavyCard
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (ach.isUnlocked) GoldPrimary.copy(alpha = 0.5f) else SlateBorder.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (ach.isUnlocked) GoldPrimary else SlateBorder.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) DarkNavyBase else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) TextWhite else TextLight
                            )
                        )
                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (ach.isUnlocked) GoldPrimary.copy(alpha = 0.2f) else DarkNavySurface
                    ) {
                        Text(
                            text = "+₺${Formatters.formatLiraCompact(ach.rewardTry)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (ach.isUnlocked) GoldLight else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
