package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    DONOR,
    RECEIVER,
    ADMIN
}

enum class DonationStatus {
    AVAILABLE,
    CLAIMED,
    COMPLETED
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    COMPLETED,
    REJECTED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String = "123456",
    val role: String = UserRole.DONOR.name,
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "donations")
data class FoodDonationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val donorId: Long,
    val donorName: String,
    val donorPhone: String,
    val foodName: String,
    val category: String,
    val quantity: String,
    val pickupLocation: String,
    val expiryDate: String,
    val expiryTimestamp: Long,
    val description: String,
    val status: String = DonationStatus.AVAILABLE.name,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requests")
data class FoodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val donationId: Long,
    val foodName: String,
    val foodCategory: String,
    val donorId: Long,
    val donorName: String,
    val receiverId: Long,
    val receiverName: String,
    val receiverPhone: String,
    val requestedQuantity: String,
    val pickupLocation: String,
    val note: String = "",
    val status: String = RequestStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
)

data class AiRecommendation(
    val donationId: Long,
    val foodName: String,
    val matchScorePercent: Int,
    val urgencyLevel: String, // "CRITICAL", "HIGH", "MODERATE"
    val reason: String,
    val foodSafetyTips: String
)
