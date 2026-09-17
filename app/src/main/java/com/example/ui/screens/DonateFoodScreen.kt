package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateFoodScreen(
    donorAddress: String,
    onSubmitDonation: (
        foodName: String,
        category: String,
        quantity: String,
        pickupLocation: String,
        expiryDate: String,
        hoursUntilExpiry: Long,
        description: String
    ) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cooked Meals") }
    var quantity by remember { mutableStateOf("") }
    var pickupLocation by remember { mutableStateOf(donorAddress.ifBlank { "Downtown Community Center" }) }
    var expiryDate by remember { mutableStateOf("Today, 10:00 PM") }
    var hoursUntilExpiry by remember { mutableStateOf(8L) }
    var description by remember { mutableStateOf("") }

    // Validation error states
    var foodNameError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var expiryError by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Cooked Meals",
        "Bakery & Bread",
        "Fresh Produce",
        "Dairy & Eggs",
        "Canned & Packaged",
        "Beverages"
    )

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("donate_food_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = ForestGreen.copy(alpha = 0.08f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Donate Surplus Food",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "List fresh surplus meals or produce to help people in need.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Food Name Field
        Column {
            Text(
                text = "Food Name *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = foodName,
                onValueChange = {
                    foodName = it
                    foodNameError = null
                },
                placeholder = { Text("e.g., Vegetable Fried Rice, Artisan Sourdough") },
                leadingIcon = {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                },
                isError = foodNameError != null,
                supportingText = {
                    foodNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donation_food_name_input")
            )
        }

        // Category Dropdown
        Column {
            Text(
                text = "Food Category *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("donation_category_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Quantity Field
        Column {
            Text(
                text = "Quantity Available *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    quantityError = null
                },
                placeholder = { Text("e.g., 20 boxes, 10 kg, 15 packages") },
                leadingIcon = {
                    Icon(Icons.Default.Scale, contentDescription = null)
                },
                isError = quantityError != null,
                supportingText = {
                    quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donation_quantity_input")
            )
        }

        // Pickup Location Field
        Column {
            Text(
                text = "Pickup Location *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = pickupLocation,
                onValueChange = {
                    pickupLocation = it
                    locationError = null
                },
                placeholder = { Text("e.g., 420 Market St, Downtown Bakery Side Entrance") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                isError = locationError != null,
                supportingText = {
                    locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donation_location_input")
            )
        }

        // Expiry Date & Freshness Window
        Column {
            Text(
                text = "Expiry Date & Time *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = expiryDate,
                onValueChange = {
                    expiryDate = it
                    expiryError = null
                },
                placeholder = { Text("e.g., Today, 9:00 PM or Tomorrow 2:00 PM") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                },
                isError = expiryError != null,
                supportingText = {
                    expiryError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donation_expiry_input")
            )

            // Quick expiry timing presets
            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {
                        expiryDate = "Today in 4 hours"
                        hoursUntilExpiry = 4L
                    },
                    label = { Text("In 4 hrs (Urgent)") }
                )
                SuggestionChip(
                    onClick = {
                        expiryDate = "Tonight, 11:00 PM"
                        hoursUntilExpiry = 10L
                    },
                    label = { Text("Tonight") }
                )
                SuggestionChip(
                    onClick = {
                        expiryDate = "Tomorrow, 6:00 PM"
                        hoursUntilExpiry = 24L
                    },
                    label = { Text("Tomorrow (24h)") }
                )
                SuggestionChip(
                    onClick = {
                        expiryDate = "In 3 Days"
                        hoursUntilExpiry = 72L
                    },
                    label = { Text("In 3 Days") }
                )
            }
        }

        // Description / Storage Notes Field
        Column {
            Text(
                text = "Description & Handling Instructions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("e.g., Prepared fresh at 2 PM, vegetarian, keep refrigerated, sealed packaging.") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donation_description_input")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = {
                var hasError = false
                if (foodName.isBlank()) {
                    foodNameError = "Please enter the food name."
                    hasError = true
                }
                if (quantity.isBlank()) {
                    quantityError = "Please specify quantity."
                    hasError = true
                }
                if (pickupLocation.isBlank()) {
                    locationError = "Please provide pickup location."
                    hasError = true
                }
                if (expiryDate.isBlank()) {
                    expiryError = "Please specify expiry date."
                    hasError = true
                }

                if (!hasError) {
                    onSubmitDonation(
                        foodName,
                        category,
                        quantity,
                        pickupLocation,
                        expiryDate,
                        hoursUntilExpiry,
                        description
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_donation_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolunteerActivism,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Publish Food Donation",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
