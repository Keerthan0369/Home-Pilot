package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ApprovalItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(
    approvals: List<ApprovalItem>,
    currentUserRole: String, // Ensure role check - only Admin/Co-admin can grant response!
    onResolveApproval: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Pending") } // Pending, History

    val filtered = approvals.filter {
        if (selectedFilter == "Pending") it.status == "PENDING" else it.status != "PENDING"
    }

    val isAdmin = currentUserRole == "Admin" || currentUserRole == "Co-admin"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- 1. Header Signoff ---
        Text(
            text = "Household Signoffs",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Approve sensitive actions, credentials view & shared vaults.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Filter switches ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "Pending",
                onClick = { selectedFilter = "Pending" },
                label = { Text("Pending Action (${approvals.count { it.status == "PENDING" }})") }
            )
            FilterChip(
                selected = selectedFilter == "History",
                onClick = { selectedFilter = "History" },
                label = { Text("Audit logs history") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Non-admin prompt alert warning ---
        if (!isAdmin && selectedFilter == "Pending" && filtered.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Operating as: '$currentUserRole'. Only Household Admins can process pending approvals. Switch profiles in the top bar to verify admin features.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 4. Roster List ---
        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "No Signoffs Pending",
                message = "Your household queue is completely verified. All joint family folders are securely synched.",
                icon = Icons.Default.VerifiedUser
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { item ->
                    ApprovalRequestCard(
                        item = item,
                        canProcess = isAdmin,
                        onAction = { approve -> onResolveApproval(item.id, approve) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalRequestCard(
    item: ApprovalItem,
    canProcess: Boolean,
    onAction: (Boolean) -> Unit
) {
    val df = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    HomePilotCard(backgroundColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                     text = item.title,
                     fontSize = 15.sp,
                     fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                     color = when (item.status) {
                         "PENDING" -> Color(0xFFFFF3CD)
                         "APPROVED" -> Color(0xFFD4EDDA)
                         else -> Color(0xFFF8D7DA)
                     },
                     shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                         text = item.status,
                         fontSize = 10.sp,
                         fontWeight = FontWeight.Bold,
                         color = when (item.status) {
                             "PENDING" -> Color(0xFF856404)
                             "APPROVED" -> Color(0xFF155724)
                             else -> Color(0xFF721C24)
                         },
                         modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                 text = item.details,
                 fontSize = 12.sp,
                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider()

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By: ${item.requesterName} at ${df.format(Date(item.timestamp))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (item.status == "PENDING") {
                    if (canProcess) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAction(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 11.sp, color = Color(0xFFC62828))
                            }

                            Button(
                                onClick = { onAction(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8E6C9)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", fontSize = 11.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    } else {
                        Text(
                             text = "Admin signoff required",
                             fontSize = 11.sp,
                             color = Color.Gray,
                             fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (item.decisionBy != null) {
                    Text(
                         text = "Signed off by: ${item.decisionBy} ${item.decisionTime?.let { "on " + df.format(Date(it)) } ?: ""}",
                         fontSize = 10.sp,
                         fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
