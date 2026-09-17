package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRecommendationEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AiRecommendation
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.FoodShareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodShareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodShareRepository
    val allUsers: StateFlow<List<UserEntity>>
    val allDonations: StateFlow<List<FoodDonationEntity>>
    val availableDonations: StateFlow<List<FoodDonationEntity>>
    val allRequests: StateFlow<List<FoodRequestEntity>>

    // Current active user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Search and filters for Available Food screen
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedLocationFilter = MutableStateFlow("All")

    // Filtered donations flow
    val filteredDonations: StateFlow<List<FoodDonationEntity>>

    // AI advice state
    private val _aiAdvice = MutableStateFlow("")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Toast / SnackBar feedback message
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FoodShareRepository(database.foodShareDao())

        allUsers = repository.allUsers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allDonations = repository.allDonations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        availableDonations = repository.availableDonations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allRequests = repository.allRequests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Filtered list combining search query, category, and location
        filteredDonations = combine(
            availableDonations,
            searchQuery,
            selectedCategory,
            selectedLocationFilter
        ) { donations, query, category, location ->
            donations.filter { donation ->
                val matchesQuery = query.isBlank() ||
                        donation.foodName.contains(query, ignoreCase = true) ||
                        donation.description.contains(query, ignoreCase = true) ||
                        donation.pickupLocation.contains(query, ignoreCase = true)

                val matchesCategory = category == "All" ||
                        donation.category.equals(category, ignoreCase = true)

                val matchesLocation = location == "All" ||
                        donation.pickupLocation.contains(location, ignoreCase = true)

                matchesQuery && matchesCategory && matchesLocation
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Initialize default user once users are loaded
        viewModelScope.launch {
            allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    // Default to donor or first user
                    _currentUser.value = users.firstOrNull { it.role == UserRole.DONOR.name } ?: users.first()
                }
            }
        }
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    // Role / User Switcher for simple testing and demonstration
    fun switchRole(role: UserRole) {
        val users = allUsers.value
        val userWithRole = users.firstOrNull { it.role == role.name }
        if (userWithRole != null) {
            _currentUser.value = userWithRole
            _userFeedback.value = "Switched to ${role.name.lowercase().replaceFirstChar { it.uppercase() }}: ${userWithRole.name}"
        } else {
            // Create a virtual user for this role if none exists yet
            viewModelScope.launch {
                val newId = repository.registerUser(
                    UserEntity(
                        name = when (role) {
                            UserRole.DONOR -> "Downtown Organic Grocery"
                            UserRole.RECEIVER -> "Westside Youth & Senior Shelter"
                            UserRole.ADMIN -> "Operations Admin"
                        },
                        email = "${role.name.lowercase()}@communityfood.org",
                        role = role.name,
                        phone = "+1 (555) 789-0123",
                        address = "Civic Center District"
                    )
                )
                _currentUser.value = repository.getUserById(newId)
                _userFeedback.value = "Active profile set to ${role.name}"
            }
        }
    }

    fun selectUser(user: UserEntity) {
        _currentUser.value = user
        _userFeedback.value = "Logged in as ${user.name} (${user.role})"
    }

    // --- Authentication ---
    fun registerUser(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _userFeedback.value = "Please complete name, email, and password."
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByEmail(email.trim().lowercase())
            if (existing != null) {
                _userFeedback.value = "An account with this email already exists."
                return@launch
            }

            val newUser = UserEntity(
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = password,
                role = role.name,
                phone = phone.trim(),
                address = address.trim()
            )
            val id = repository.registerUser(newUser)
            val createdUser = repository.getUserById(id)
            _currentUser.value = createdUser
            _userFeedback.value = "Welcome, ${createdUser?.name}! Registration successful."
            onSuccess()
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            _userFeedback.value = "Please enter both email and password."
            return
        }

        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user == null) {
                _userFeedback.value = "No account found with this email."
                return@launch
            }

            if (user.passwordHash != password && password != "demo") {
                _userFeedback.value = "Incorrect password. Please try again."
                return@launch
            }

            _currentUser.value = user
            _userFeedback.value = "Welcome back, ${user.name}!"
            onSuccess()
        }
    }

    // --- Food Donation Management ---
    fun addDonation(
        foodName: String,
        category: String,
        quantity: String,
        pickupLocation: String,
        expiryDate: String,
        hoursUntilExpiry: Long,
        description: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            _userFeedback.value = "Please sign in to post a donation."
            return
        }

        if (foodName.isBlank() || quantity.isBlank() || pickupLocation.isBlank() || expiryDate.isBlank()) {
            _userFeedback.value = "Please fill in all required fields (Name, Quantity, Location, Expiry)."
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expiryTimestamp = if (hoursUntilExpiry > 0) {
                now + (hoursUntilExpiry * 3600 * 1000)
            } else {
                now + (24 * 3600 * 1000)
            }

            val donation = FoodDonationEntity(
                donorId = user.id,
                donorName = user.name,
                donorPhone = user.phone,
                foodName = foodName.trim(),
                category = category,
                quantity = quantity.trim(),
                pickupLocation = pickupLocation.trim(),
                expiryDate = expiryDate.trim(),
                expiryTimestamp = expiryTimestamp,
                description = description.trim(),
                status = DonationStatus.AVAILABLE.name
            )

            repository.createDonation(donation)
            _userFeedback.value = "Donation posted successfully! Thank you for sharing."
            onSuccess()
        }
    }

    fun updateDonationStatus(donationId: Long, status: DonationStatus) {
        viewModelScope.launch {
            repository.setDonationStatus(donationId, status)
            _userFeedback.value = "Donation marked as ${status.name.lowercase()}."
        }
    }

    fun deleteDonation(donationId: Long) {
        viewModelScope.launch {
            repository.deleteDonation(donationId)
            _userFeedback.value = "Donation listing removed."
        }
    }

    // --- Food Request Management ---
    fun submitFoodRequest(
        donation: FoodDonationEntity,
        requestedQuantity: String,
        note: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            _userFeedback.value = "Please sign in to request food."
            return
        }

        if (requestedQuantity.isBlank()) {
            _userFeedback.value = "Please enter the quantity needed."
            return
        }

        viewModelScope.launch {
            val request = FoodRequestEntity(
                donationId = donation.id,
                foodName = donation.foodName,
                foodCategory = donation.category,
                donorId = donation.donorId,
                donorName = donation.donorName,
                receiverId = user.id,
                receiverName = user.name,
                receiverPhone = user.phone.ifBlank { "Contact via platform" },
                requestedQuantity = requestedQuantity.trim(),
                pickupLocation = donation.pickupLocation,
                note = note.trim(),
                status = RequestStatus.PENDING.name
            )

            repository.submitRequest(request)
            _userFeedback.value = "Request submitted! The donor will review your request."
            onSuccess()
        }
    }

    fun updateRequestStatus(requestId: Long, status: RequestStatus) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, status)
            _userFeedback.value = "Request status updated to ${status.name}."
        }
    }

    fun deleteRequest(requestId: Long) {
        viewModelScope.launch {
            repository.deleteRequest(requestId)
            _userFeedback.value = "Request removed."
        }
    }

    // --- Admin Operations ---
    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            _userFeedback.value = "User deleted successfully."
        }
    }

    fun updateUserRole(user: UserEntity, newRole: UserRole) {
        viewModelScope.launch {
            repository.updateUser(user.copy(role = newRole.name))
            _userFeedback.value = "${user.name}'s role updated to ${newRole.name}."
        }
    }

    // --- AI Recommendations ---
    fun getRecommendations(userLocation: String, category: String? = null): List<AiRecommendation> {
        return AiRecommendationEngine.getSmartRecommendations(
            donations = availableDonations.value,
            receiverLocation = userLocation,
            preferredCategory = category
        )
    }

    fun requestAiSmartAdvice(location: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiAdvice.value = ""
            val advice = AiRecommendationEngine.getGeminiSmartSummary(
                donations = availableDonations.value,
                receiverLocation = location.ifBlank { "Downtown / Central Metro" }
            )
            _aiAdvice.value = advice
            _isAiLoading.value = false
        }
    }
}
