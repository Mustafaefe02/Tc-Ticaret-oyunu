package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.OnlineHoldingEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MultiplayerRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Kullanıcı Google ile giriş yapmalıdır.")
    }

    /**
     * Real-time stream of all top online player holdings ordered by net worth descending.
     */
    fun observeLeaderboard(): Flow<List<OnlineHoldingEntry>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val query = db.collection("holdings")
            .orderBy("netWorth", Query.Direction.DESCENDING)
            .limit(50)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(OnlineHoldingEntry::class.java)
                    } catch (e: Exception) {
                        Log.e("Multiplayer", "Parse error for ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e("Multiplayer", "Leaderboard observe error: ${e.message}", e)
                emit(emptyList())
            }
            .collect { list ->
                emit(list)
            }
    }

    /**
     * Publish or update the player's holding on the global multiplayer leaderboard.
     */
    suspend fun publishHoldingStats(
        holdingName: String,
        ownerName: String,
        netWorth: Double,
        cash: Double,
        companyCount: Int,
        totalEmployees: Int,
        prestige: Int
    ) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("holdings").document(uid)

        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updateData = mapOf(
                    "userId" to uid,
                    "holdingName" to holdingName.ifBlank { "Holding" },
                    "ownerName" to ownerName.ifBlank { "Oyuncu" },
                    "netWorth" to netWorth,
                    "cash" to cash,
                    "companyCount" to companyCount,
                    "totalEmployees" to totalEmployees,
                    "prestige" to prestige,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateData).await()
            } else {
                val createData = mapOf(
                    "userId" to uid,
                    "holdingName" to holdingName.ifBlank { "Holding" },
                    "ownerName" to ownerName.ifBlank { "Oyuncu" },
                    "netWorth" to netWorth,
                    "cash" to cash,
                    "companyCount" to companyCount,
                    "totalEmployees" to totalEmployees,
                    "prestige" to prestige,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createData).await()
            }
        } catch (e: Exception) {
            Log.e("Multiplayer", "Failed to sync holding stats: ${e.message}", e)
        }
    }
}
