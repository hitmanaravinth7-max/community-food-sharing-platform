package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodRequestEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodShareDao {

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // --- Food Donations ---
    @Query("SELECT * FROM donations ORDER BY createdAt DESC")
    fun getAllDonations(): Flow<List<FoodDonationEntity>>

    @Query("SELECT * FROM donations WHERE status = 'AVAILABLE' ORDER BY expiryTimestamp ASC")
    fun getAvailableDonations(): Flow<List<FoodDonationEntity>>

    @Query("SELECT * FROM donations WHERE donorId = :donorId ORDER BY createdAt DESC")
    fun getDonationsByDonor(donorId: Long): Flow<List<FoodDonationEntity>>

    @Query("SELECT * FROM donations WHERE id = :id LIMIT 1")
    suspend fun getDonationById(id: Long): FoodDonationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: FoodDonationEntity): Long

    @Update
    suspend fun updateDonation(donation: FoodDonationEntity)

    @Query("UPDATE donations SET status = :status WHERE id = :id")
    suspend fun updateDonationStatus(id: Long, status: String)

    @Query("DELETE FROM donations WHERE id = :id")
    suspend fun deleteDonation(id: Long)

    @Query("SELECT COUNT(*) FROM donations")
    suspend fun getDonationCount(): Int

    // --- Food Requests ---
    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM requests WHERE receiverId = :receiverId ORDER BY createdAt DESC")
    fun getRequestsByReceiver(receiverId: Long): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM requests WHERE donorId = :donorId ORDER BY createdAt DESC")
    fun getRequestsByDonor(donorId: Long): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM requests WHERE donationId = :donationId ORDER BY createdAt DESC")
    fun getRequestsForDonation(donationId: Long): Flow<List<FoodRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: FoodRequestEntity): Long

    @Update
    suspend fun updateRequest(request: FoodRequestEntity)

    @Query("UPDATE requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String)

    @Query("DELETE FROM requests WHERE id = :id")
    suspend fun deleteRequest(id: Long)

    @Query("SELECT COUNT(*) FROM requests")
    suspend fun getRequestCount(): Int
}
