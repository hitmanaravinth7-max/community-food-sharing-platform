package com.example.data.repository

import com.example.data.local.FoodShareDao
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class FoodShareRepository(private val dao: FoodShareDao) {

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)

    suspend fun getUserById(id: Long): UserEntity? = dao.getUserById(id)

    suspend fun registerUser(user: UserEntity): Long = dao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    suspend fun deleteUser(id: Long) = dao.deleteUser(id)

    // --- Food Donations ---
    val allDonations: Flow<List<FoodDonationEntity>> = dao.getAllDonations()

    val availableDonations: Flow<List<FoodDonationEntity>> = dao.getAvailableDonations()

    fun getDonationsByDonor(donorId: Long): Flow<List<FoodDonationEntity>> =
        dao.getDonationsByDonor(donorId)

    suspend fun getDonationById(id: Long): FoodDonationEntity? = dao.getDonationById(id)

    suspend fun createDonation(donation: FoodDonationEntity): Long = dao.insertDonation(donation)

    suspend fun updateDonation(donation: FoodDonationEntity) = dao.updateDonation(donation)

    suspend fun setDonationStatus(id: Long, status: DonationStatus) =
        dao.updateDonationStatus(id, status.name)

    suspend fun deleteDonation(id: Long) = dao.deleteDonation(id)

    // --- Food Requests ---
    val allRequests: Flow<List<FoodRequestEntity>> = dao.getAllRequests()

    fun getRequestsByReceiver(receiverId: Long): Flow<List<FoodRequestEntity>> =
        dao.getRequestsByReceiver(receiverId)

    fun getRequestsByDonor(donorId: Long): Flow<List<FoodRequestEntity>> =
        dao.getRequestsByDonor(donorId)

    fun getRequestsForDonation(donationId: Long): Flow<List<FoodRequestEntity>> =
        dao.getRequestsForDonation(donationId)

    suspend fun submitRequest(request: FoodRequestEntity): Long = dao.insertRequest(request)

    suspend fun updateRequestStatus(id: Long, status: RequestStatus) =
        dao.updateRequestStatus(id, status.name)

    suspend fun deleteRequest(id: Long) = dao.deleteRequest(id)
}
