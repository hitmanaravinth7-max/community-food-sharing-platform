package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationStatus
import com.example.data.model.FoodDonationEntity
import com.example.data.model.FoodRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UserEntity
import com.example.ui.components.DonationStatusBadge
import com.example.ui.components.RequestStatusBadge
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.HarvestAmber
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusCompleted

@Composable
fun DashboardScreen(
    currentUser: UserEntity?,
    allDonations: List<FoodDonationEntity>,
    allRequests: List<FoodRequestEntity>,
    onRequestStatusChange: (Long, RequestStatus) -> Unit,
    onDonationStatusChange: (Long, DonationStatus) -> Unit,
    onNavigateToDonate: () -> Unit,
    onNavigateToAvailable: () -> Unit
) {
    val isDonor = currentUser?.role == "DONOR"
    val userId = currentUser?.id ?: 0L

    // For Donor: their donations and requests directed to them
    val myDonations = allDonations.filter { it.donorId == userId }
    val incomingRequests = allRequests.filter { it.donorId == userId }

    // For Receiver: their submitted requests
    val myRequests = allRequests.filter { it.receiverId == userId }

    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isDonor) ForestGreen.copy(alpha = 0.15f) else HarvestAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDonor) Icons.Default.VolunteerActivism else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isDonor) ForestGreen else HarvestAmber,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Community Member",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser?.role ?: "User"} • ${currentUser?.address ?: "Metro Region"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isDonor) {
                    MetricCard(
                        title = "Total Donations",
                        value = "${myDonations.size}",
                        color = ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Available",
                        value = "${myDonations.count { it.status == DonationStatus.AVAILABLE.name }}",
                        color = HarvestAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Incoming Requests",
                        value = "${incomingRequests.size}",
                        color = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MetricCard(
                        title = "My Requests",
                        value = "${myRequests.size}",
                        color = ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Approved Pickups",
                        value = "${myRequests.count { it.status == RequestStatus.APPROVED.name }}",
                        color = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Completed Meals",
                        value = "${myRequests.count { it.status == RequestStatus.COMPLETED.name }}",
                        color = StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tabs
        item {
            val tabs = if (isDonor) {
                listOf("Incoming Requests (${incomingRequests.size})", "My Donations (${myDonations.size})")
            } else {
                listOf("My Requests (${myRequests.size})", "Available Food")
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    )
                }
            }
        }

        // Tab Content
        if (isDonor) {
            if (selectedTab == 0) {
                // Donor: Incoming Requests from Receivers
                if (incomingRequests.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = "No incoming food requests at the moment.",
                            actionText = "Post More Surplus",
                            onAction = onNavigateToDonate
                        )
                    }
                } else {
                    items(incomingRequests, key = { it.id }) { request ->
                        IncomingRequestCard(
                            request = request,
                            onApprove = { onRequestStatusChange(request.id, RequestStatus.APPROVED) },
                            onComplete = { onRequestStatusChange(request.id, RequestStatus.COMPLETED) },
                            onReject = { onRequestStatusChange(request.id, RequestStatus.REJECTED) }
                        )
                    }
                }
            } else {
                // Donor: My Donations History
                if (myDonations.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = "You haven't posted any food donations yet.",
                            actionText = "+ Donate Food",
                            onAction = onNavigateToDonate
                        )
                    }
                } else {
                    items(myDonations, key = { it.id }) { donation ->
                        DonorDonationItemCard(
                            donation = donation,
                            onToggleStatus = { newStatus ->
                                onDonationStatusChange(donation.id, newStatus)
                            }
                        )
                    }
                }
            }
        } else {
            // Receiver Views
            if (selectedTab == 0) {
                // Receiver: My Request History
                if (myRequests.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = "You haven't requested any food yet.",
                            actionText = "Find Available Food",
                            onAction = onNavigateToAvailable
                        )
                    }
                } else {
                    items(myRequests, key = { it.id }) { request ->
                        ReceiverRequestCard(request = request)
                    }
                }
            } else {
                // Receiver: Quick Available Food Snapshot
                items(allDonations.filter { it.status == DonationStatus.AVAILABLE.name }) { donation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(donation.foodName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${donation.quantity} • ${donation.pickupLocation}", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = onNavigateToAvailable,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun IncomingRequestCard(
    request: FoodRequestEntity,
    onApprove: () -> Unit,
    onComplete: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RequestStatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Requested by: ${request.receiverName} (${request.requestedQuantity})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (request.note.isNotBlank()) {
                Text(
                    text = "Note: \"${request.note}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (request.status == RequestStatus.PENDING.name) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Decline", fontSize = 12.sp, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Request", fontSize = 12.sp)
                    }
                } else if (request.status == RequestStatus.APPROVED.name) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Handed Over", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiverRequestCard(request: FoodRequestEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RequestStatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Donor: ${request.donorName}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Quantity: ${request.requestedQuantity} • ${request.foodCategory}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = request.pickupLocation, style = MaterialTheme.typography.bodySmall)
            }

            if (request.status == RequestStatus.APPROVED.name) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusApproved.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Ready for pickup! Please coordinate arrival at the specified location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusApproved,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DonorDonationItemCard(
    donation: FoodDonationEntity,
    onToggleStatus: (DonationStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = donation.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                DonationStatusBadge(status = donation.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${donation.quantity} • ${donation.category} • Expiry: ${donation.expiryDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (donation.status == DonationStatus.AVAILABLE.name) {
                    OutlinedButton(
                        onClick = { onToggleStatus(DonationStatus.COMPLETED) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Given", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onToggleStatus(DonationStatus.AVAILABLE) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Re-activate", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaceholder(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionText, fontSize = 13.sp)
            }
        }
    }
}
