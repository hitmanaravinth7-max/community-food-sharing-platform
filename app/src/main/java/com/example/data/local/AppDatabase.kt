package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, FoodDonationEntity::class, FoodRequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodShareDao(): FoodShareDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_share_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.foodShareDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: FoodShareDao) {
                // Seed Users
                val donor1Id = dao.insertUser(
                    UserEntity(
                        name = "Green Hearth Bakery",
                        email = "bakery@foodshare.org",
                        passwordHash = "password123",
                        role = UserRole.DONOR.name,
                        phone = "+1 (555) 234-5678",
                        address = "420 Market St, Downtown"
                    )
                )

                val donor2Id = dao.insertUser(
                    UserEntity(
                        name = "Sunrise Community Cafe",
                        email = "cafe@foodshare.org",
                        passwordHash = "password123",
                        role = UserRole.DONOR.name,
                        phone = "+1 (555) 345-6789",
                        address = "188 Oak Ave, Midtown"
                    )
                )

                val receiver1Id = dao.insertUser(
                    UserEntity(
                        name = "Hope Haven Community Shelter",
                        email = "receiver@foodshare.org",
                        passwordHash = "password123",
                        role = UserRole.RECEIVER.name,
                        phone = "+1 (555) 987-6543",
                        address = "750 Pine Street, Westside"
                    )
                )

                dao.insertUser(
                    UserEntity(
                        name = "System Administrator",
                        email = "admin@foodshare.org",
                        passwordHash = "admin123",
                        role = UserRole.ADMIN.name,
                        phone = "+1 (555) 000-1111",
                        address = "Food Share HQ, Civic Center"
                    )
                )

                // Current time reference
                val now = System.currentTimeMillis()
                val sixHoursFromNow = now + (6 * 3600 * 1000)
                val oneDayFromNow = now + (24 * 3600 * 1000)
                val twoDaysFromNow = now + (48 * 3600 * 1000)
                val fiveDaysFromNow = now + (120 * 3600 * 1000)

                // Seed Donations
                val donation1Id = dao.insertDonation(
                    FoodDonationEntity(
                        donorId = donor1Id,
                        donorName = "Green Hearth Bakery",
                        donorPhone = "+1 (555) 234-5678",
                        foodName = "Artisan Sourdough & Croissants",
                        category = "Bakery & Bread",
                        quantity = "18 loaves & 24 pastries",
                        pickupLocation = "420 Market St, Downtown (Side Delivery Door)",
                        expiryDate = "Today, 9:00 PM",
                        expiryTimestamp = sixHoursFromNow,
                        description = "Freshly baked morning sourdough and butter croissants, securely packaged. Suitable for vegetarians.",
                        status = DonationStatus.AVAILABLE.name,
                        createdAt = now - 7200000
                    )
                )

                val donation2Id = dao.insertDonation(
                    FoodDonationEntity(
                        donorId = donor2Id,
                        donorName = "Sunrise Community Cafe",
                        donorPhone = "+1 (555) 345-6789",
                        foodName = "Vegetable Biryani & Lentil Dahl",
                        category = "Cooked Meals",
                        quantity = "30 individual meal boxes",
                        pickupLocation = "188 Oak Ave, Midtown (Kitchen Entrance)",
                        expiryDate = "Today, 11:30 PM",
                        expiryTimestamp = sixHoursFromNow + 7200000,
                        description = "Hot packaged nutritious vegetarian meals prepared for an afternoon event. Sealed in food-grade eco containers.",
                        status = DonationStatus.AVAILABLE.name,
                        createdAt = now - 3600000
                    )
                )

                val donation3Id = dao.insertDonation(
                    FoodDonationEntity(
                        donorId = donor2Id,
                        donorName = "Sunrise Community Cafe",
                        donorPhone = "+1 (555) 345-6789",
                        foodName = "Fresh Organic Apples & Bananas",
                        category = "Fresh Produce",
                        quantity = "15 kg fruit crate",
                        pickupLocation = "188 Oak Ave, Midtown",
                        expiryDate = "In 2 Days",
                        expiryTimestamp = twoDaysFromNow,
                        description = "Crisp sweet apples and ripe bananas donated from farmer co-op deliveries. Great for family packs.",
                        status = DonationStatus.AVAILABLE.name,
                        createdAt = now - 18000000
                    )
                )

                val donation4Id = dao.insertDonation(
                    FoodDonationEntity(
                        donorId = donor1Id,
                        donorName = "Green Hearth Bakery",
                        donorPhone = "+1 (555) 234-5678",
                        foodName = "Pasteurized Whole Milk & Yogurt",
                        category = "Dairy & Eggs",
                        quantity = "20 cartons milk & 15 yogurts",
                        pickupLocation = "420 Market St, Downtown",
                        expiryDate = "Tomorrow, 6:00 PM",
                        expiryTimestamp = oneDayFromNow,
                        description = "Refrigerated dairy surplus from local dairy partners. Must maintain cold chain during transport.",
                        status = DonationStatus.AVAILABLE.name,
                        createdAt = now - 10000000
                    )
                )

                val donation5Id = dao.insertDonation(
                    FoodDonationEntity(
                        donorId = donor1Id,
                        donorName = "Green Hearth Bakery",
                        donorPhone = "+1 (555) 234-5678",
                        foodName = "Assorted Canned Veggies & Soup",
                        category = "Canned & Packaged",
                        quantity = "35 sealed cans",
                        pickupLocation = "420 Market St, Downtown",
                        expiryDate = "In 30 Days",
                        expiryTimestamp = fiveDaysFromNow,
                        description = "Tomato soup, mixed beans, and sweet corn. Long shelf life, non-perishable.",
                        status = DonationStatus.AVAILABLE.name,
                        createdAt = now - 86400000
                    )
                )

                // Seed Requests
                dao.insertRequest(
                    FoodRequestEntity(
                        donationId = donation1Id,
                        foodName = "Artisan Sourdough & Croissants",
                        foodCategory = "Bakery & Bread",
                        donorId = donor1Id,
                        donorName = "Green Hearth Bakery",
                        receiverId = receiver1Id,
                        receiverName = "Hope Haven Community Shelter",
                        receiverPhone = "+1 (555) 987-6543",
                        requestedQuantity = "10 loaves",
                        pickupLocation = "420 Market St, Downtown",
                        note = "For our evening soup kitchen dinner service serving 40 unhoused individuals.",
                        status = RequestStatus.APPROVED.name,
                        createdAt = now - 3600000
                    )
                )

                dao.insertRequest(
                    FoodRequestEntity(
                        donationId = donation2Id,
                        foodName = "Vegetable Biryani & Lentil Dahl",
                        foodCategory = "Cooked Meals",
                        donorId = donor2Id,
                        donorName = "Sunrise Community Cafe",
                        receiverId = receiver1Id,
                        receiverName = "Hope Haven Community Shelter",
                        receiverPhone = "+1 (555) 987-6543",
                        requestedQuantity = "15 meal boxes",
                        pickupLocation = "188 Oak Ave, Midtown",
                        note = "Our volunteer van can pick up at 7:30 PM.",
                        status = RequestStatus.PENDING.name,
                        createdAt = now - 1800000
                    )
                )
            }
        }
    }
}
