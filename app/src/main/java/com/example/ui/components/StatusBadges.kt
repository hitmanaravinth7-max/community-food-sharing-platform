package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusUrgent

@Composable
fun DonationStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status.uppercase()) {
        "AVAILABLE" -> Triple(StatusAvailable.copy(alpha = 0.15f), StatusAvailable, "Available")
        "CLAIMED" -> Triple(StatusPending.copy(alpha = 0.15f), StatusPending, "Claimed / In Transit")
        "COMPLETED" -> Triple(StatusCompleted.copy(alpha = 0.15f), StatusCompleted, "Shared / Completed")
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RequestStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon, label) = when (status.uppercase()) {
        "PENDING" -> Quadruple(
            StatusPending.copy(alpha = 0.15f),
            StatusPending,
            Icons.Default.HourglassEmpty,
            "Pending Review"
        )
        "APPROVED" -> Quadruple(
            StatusApproved.copy(alpha = 0.15f),
            StatusApproved,
            Icons.Default.CheckCircle,
            "Approved for Pickup"
        )
        "COMPLETED" -> Quadruple(
            StatusCompleted.copy(alpha = 0.15f),
            StatusCompleted,
            Icons.Default.CheckCircle,
            "Received & Completed"
        )
        "REJECTED" -> Quadruple(
            StatusUrgent.copy(alpha = 0.15f),
            StatusUrgent,
            Icons.Default.Warning,
            "Declined"
        )
        else -> Quadruple(
            Color.Gray.copy(alpha = 0.15f),
            Color.DarkGray,
            Icons.Default.Schedule,
            status
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ExpiryAlertBadge(
    expiryTimestamp: Long,
    expiryDateString: String,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val hoursLeft = (expiryTimestamp - now) / (1000 * 3600)

    val (bgColor, textColor, icon, label) = when {
        hoursLeft <= 6 -> Quadruple(
            StatusUrgent.copy(alpha = 0.15f),
            StatusUrgent,
            Icons.Default.Warning,
            "Urgent: ~$hoursLeft hrs left"
        )
        hoursLeft <= 24 -> Quadruple(
            Color(0xFFE65100).copy(alpha = 0.15f),
            Color(0xFFE65100),
            Icons.Default.Schedule,
            "Expires today ($expiryDateString)"
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Schedule,
            expiryDateString
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Expiry icon",
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
