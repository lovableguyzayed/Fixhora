package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE taskId = :taskId ORDER BY amount ASC, createdAt ASC")
    fun offersForTask(taskId: Int): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers ORDER BY createdAt DESC")
    fun allOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE taskId = :taskId AND helperId = :helperId LIMIT 1")
    suspend fun offerBy(taskId: Int, helperId: String): OfferEntity?

    @Insert
    suspend fun insert(offer: OfferEntity): Long

    @Query("UPDATE offers SET status = CASE WHEN id = :offerId THEN 'accepted' ELSE 'declined' END WHERE taskId = :taskId")
    suspend fun acceptOffer(taskId: Int, offerId: Int)

    @Query("DELETE FROM offers")
    suspend fun deleteAll()
}
