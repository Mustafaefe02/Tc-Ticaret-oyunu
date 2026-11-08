package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class OnlineHoldingEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val holdingName: String = "",
    val ownerName: String = "",
    val netWorth: Double = 0.0,
    val cash: Double = 0.0,
    val companyCount: Int = 0,
    val totalEmployees: Int = 0,
    val prestige: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
