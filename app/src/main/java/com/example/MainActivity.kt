package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodDonationEntity
import com.example.data.model.UserRole
import com.example.ui.components.AppTopBar
import com.example.ui.components.RequestFoodDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiRecommendationsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AvailableFoodScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DonateFoodScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FoodShareViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FoodShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FoodShareApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "tab_home"),
    AVAILABLE("Find Food", Icons.Default.Fastfood, "tab_available"),
    DONATE("Donate", Icons.Default.VolunteerActivism, "tab_donate"),
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    AI_HUB("AI Match", Icons.Default.AutoAwesome, "tab_ai"),
    ADMIN("Admin", Icons.Default.AdminPanelSettings, "tab_admin")
}

@Composable
fun FoodShareApp(viewModel: FoodShareViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allDonations by viewModel.allDonations.collectAsStateWithLifecycle()
    val availableDonations by viewModel.availableDonations.collectAsStateWithLifecycle()
    val filteredDonations by viewModel.filteredDonations.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedLocationFilter by viewModel.selectedLocationFilter.collectAsStateWithLifecycle()

    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedback.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var selectedDonationForRequest by remember { mutableStateOf<FoodDonationEntity?>(null) }
    var showAuthModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle toast/snackbar notifications
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                currentUser = currentUser,
                onSwitchRole = { role -> viewModel.switchRole(role) },
                onOpenAuth = { showAuthModal = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val tabs = NavigationTab.values()
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = ForestGreen,
                            indicatorColor = ForestGreen.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showAuthModal) {
                AuthScreen(
                    onLogin = { email, password ->
                        viewModel.loginUser(email, password) {
                            showAuthModal = false
                        }
                    },
                    onRegister = { name, email, password, role, phone, address ->
                        viewModel.registerUser(name, email, password, role, phone, address) {
                            showAuthModal = false
                        }
                    },
                    onDismiss = { showAuthModal = false }
                )
            } else {
                when (currentTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            donations = availableDonations,
                            onNavigateToDonate = { currentTab = NavigationTab.DONATE },
                            onNavigateToAvailable = { currentTab = NavigationTab.AVAILABLE },
                            onNavigateToAiHub = { currentTab = NavigationTab.AI_HUB },
                            onRequestDonation = { donation ->
                                selectedDonationForRequest = donation
                            }
                        )
                    }

                    NavigationTab.AVAILABLE -> {
                        AvailableFoodScreen(
                            donations = filteredDonations,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            selectedCategory = selectedCategory,
                            onCategorySelect = { viewModel.selectedCategory.value = it },
                            selectedLocation = selectedLocationFilter,
                            onLocationSelect = { viewModel.selectedLocationFilter.value = it },
                            currentUserId = currentUser?.id,
                            currentUserRole = currentUser?.role,
                            onRequestFood = { donation ->
                                selectedDonationForRequest = donation
                            },
                            onStatusChange = { donationId, newStatus ->
                                viewModel.updateDonationStatus(donationId, newStatus)
                            },
                            onDeleteDonation = { donationId ->
                                viewModel.deleteDonation(donationId)
                            }
                        )
                    }

                    NavigationTab.DONATE -> {
                        DonateFoodScreen(
                            donorAddress = currentUser?.address ?: "",
                            onSubmitDonation = { foodName, category, quantity, pickupLocation, expiryDate, hoursUntilExpiry, description ->
                                viewModel.addDonation(
                                    foodName = foodName,
                                    category = category,
                                    quantity = quantity,
                                    pickupLocation = pickupLocation,
                                    expiryDate = expiryDate,
                                    hoursUntilExpiry = hoursUntilExpiry,
                                    description = description
                                ) {
                                    currentTab = NavigationTab.AVAILABLE
                                }
                            }
                        )
                    }

                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            currentUser = currentUser,
                            allDonations = allDonations,
                            allRequests = allRequests,
                            onRequestStatusChange = { requestId, status ->
                                viewModel.updateRequestStatus(requestId, status)
                            },
                            onDonationStatusChange = { donationId, status ->
                                viewModel.updateDonationStatus(donationId, status)
                            },
                            onNavigateToDonate = { currentTab = NavigationTab.DONATE },
                            onNavigateToAvailable = { currentTab = NavigationTab.AVAILABLE }
                        )
                    }

                    NavigationTab.AI_HUB -> {
                        AiRecommendationsScreen(
                            donations = availableDonations,
                            userAddress = currentUser?.address ?: "",
                            onGetRecommendations = { location, category ->
                                viewModel.getRecommendations(location, category)
                            },
                            onRequestAiAdvice = { location ->
                                viewModel.requestAiSmartAdvice(location)
                            },
                            aiAdvice = aiAdvice,
                            isAiLoading = isAiLoading,
                            onRequestDonationById = { donationId ->
                                val donation = availableDonations.firstOrNull { it.id == donationId }
                                if (donation != null) {
                                    selectedDonationForRequest = donation
                                }
                            }
                        )
                    }

                    NavigationTab.ADMIN -> {
                        AdminPanelScreen(
                            users = allUsers,
                            donations = allDonations,
                            requests = allRequests,
                            onUpdateUserRole = { user, newRole ->
                                viewModel.updateUserRole(user, newRole)
                            },
                            onDeleteUser = { userId ->
                                viewModel.deleteUser(userId)
                            },
                            onUpdateDonationStatus = { donationId, status ->
                                viewModel.updateDonationStatus(donationId, status)
                            },
                            onDeleteDonation = { donationId ->
                                viewModel.deleteDonation(donationId)
                            },
                            onUpdateRequestStatus = { requestId, status ->
                                viewModel.updateRequestStatus(requestId, status)
                            },
                            onDeleteRequest = { requestId ->
                                viewModel.deleteRequest(requestId)
                            }
                        )
                    }
                }
            }

            // Food Request Confirmation Bottom Sheet / Dialog
            selectedDonationForRequest?.let { donation ->
                RequestFoodDialog(
                    donation = donation,
                    onDismiss = { selectedDonationForRequest = null },
                    onSubmit = { quantity, note ->
                        viewModel.submitFoodRequest(donation, quantity, note) {
                            selectedDonationForRequest = null
                            currentTab = NavigationTab.DASHBOARD
                        }
                    }
                )
            }
        }
    }
}
