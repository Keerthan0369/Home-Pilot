package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*

@Composable
fun DashboardScreen(
    currentMember: FamilyMember?,
    familyMembers: List<FamilyMember> = emptyList(),
    tasks: List<TaskItem> = emptyList(),
    bills: List<BillItem> = emptyList(),
    medicines: List<MedicineItem> = emptyList(),
    approvals: List<ApprovalItem> = emptyList(),
    notifications: List<NotificationLog> = emptyList(),
    isEmergencyMode: Boolean,
    onNavigate: (route: String) -> Unit,
    onTriggerEmergency: () -> Unit,
    activities: List<FamilyActivity> = emptyList(),
    suggestions: List<SmartSuggestion> = emptyList(),
    chats: List<CompanionChatLog> = emptyList(),
    isBiometricSet: Boolean = false,
    onApproveSuggestion: (id: Int) -> Unit = {},
    onDismissSuggestion: (id: Int) -> Unit = {},
    onSendChatMessage: (message: String) -> Unit = {},
    onClearChats: () -> Unit = {},
    onSetBiometricsEnabled: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val overdueTasksCount = tasks.count { it.status == "Overdue" || (it.dueDate < System.currentTimeMillis() && it.status != "Completed") }
    val pendingBillsCount = bills.count { it.status == "Unpaid" || it.status == "Overdue" }
    val pendingBillsAmt = bills.filter { it.status == "Unpaid" || it.status == "Overdue" }.sumOf { it.amount }
    val medicinesCount = medicines.count { it.isActive && !it.isCheckedToday }
    val pendingApprovalsCount = approvals.count { it.status == "PENDING" }
    val itemsNeedAttention = overdueTasksCount + pendingBillsCount + medicinesCount + pendingApprovalsCount

    var selectedTab by remember { mutableStateOf("Quick Board") }
    var chatMessageInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF5)) // Warm sand-ivory background
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Clean Minimalist Top Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOMEPILOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Morning, ${currentMember?.name ?: "Family"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF1E293B)
                    )
                }

                // Beautiful interactive notification bell with active dot indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), CircleShape)
                        .clickable { onNavigate("family") }, // Navigates to family/notifications
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                    if (notifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .background(Color(0xFFF97316), CircleShape) // orange active indicator
                        )
                    }
                }
            }
        }

        // --- 2. Emergency Mode Banner ---
        item {
            AnimatedVisibility(visible = isEmergencyMode) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "EMERGENCY BROADCAST ACTIVE",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Care checklists prioritize remaining tasks.",
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = onTriggerEmergency,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Clear", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- 3. Dynamic Pill Mode Tabs ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf("Quick Board", "Activities", "Smart AI", "Companion AI")
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                            .clickable { selectedTab = tab }
                            .border(BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // --- 4. Tab Selective Renderer ---
        when (selectedTab) {
            "Quick Board" -> {
                // Today's Priority Emerald Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)) // Emerald-800
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "FAMILY PRIORITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = Color(0xFFA7F3D0) // Emerald-200 tint
                                )

                                Text(
                                    text = if (itemsNeedAttention > 0) "$itemsNeedAttention Items Need Attention" else "All Operating Perfectly",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (itemsNeedAttention == 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text("🌿", fontSize = 20.sp)
                                        Column {
                                            Text("Full Serenity", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Excellent! No reminders are pending right now.", color = Color(0xFFD1FAE5), fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (medicinesCount > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text("💊", fontSize = 18.sp)
                                                Column {
                                                    Text("Medicines Checklist", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text("$medicinesCount pending doses today • Check list", color = Color(0xFFD1FAE5), fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        if (pendingBillsCount > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text("⚡", fontSize = 18.sp)
                                                Column {
                                                    Text("Electricity & House Bills", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text("₹${pendingBillsAmt.toInt()} due soon • Unpaid", color = Color(0xFFD1FAE5), fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        if (overdueTasksCount > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text("✅", fontSize = 18.sp)
                                                Column {
                                                    Text("Urgent Family Tasks", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text("$overdueTasksCount items are overdue • Review now", color = Color(0xFFD1FAE5), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Action Bento Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Docs Vault Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate("documents") },
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("📂", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Docs Vault", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("Secure safe storage", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }

                            // Expenses Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate("expenses") },
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("📊", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Expenses", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("₹12k spent this month", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Family Tasks Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate("tasks") },
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("✅", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Family Tasks", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("${tasks.count { it.status == "Pending" }} tasks pending", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }

                            // Approvals Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate("approvals") },
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("🛡️", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Approvals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    if (pendingApprovalsCount > 0) {
                                        Text("$pendingApprovalsCount Requests", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                    } else {
                                        Text("0 Requests Pending", fontSize = 10.sp, color = Color(0xFF059669))
                                    }
                                }
                            }
                        }
                    }
                }

                // Family Presence
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "FAMILY PRESENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(start = 2.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(familyMembers) { member ->
                                val isSelf = member.id == currentMember?.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(member.avatarColor))
                                            .border(
                                                width = if (isSelf) 3.dp else 1.dp,
                                                color = if (isSelf) Color(0xFF059669) else Color.White,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )

                                        if (isSelf) {
                                            Box(
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color(0xFF059669), CircleShape)
                                                    .border(2.dp, Color.White, CircleShape)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isSelf) "You" else member.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }

                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clickable { onNavigate("family") }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.Transparent)
                                            .border(
                                                BorderStroke(2.dp, Brush.sweepGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Light,
                                            fontSize = 24.sp
                                        )
                                    }
                                    Text(
                                        text = "Add",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                // Compact Controls & SOS
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                QuickActionItem(icon = Icons.Default.AddHomeWork, label = "Add task") { onNavigate("tasks") }
                                QuickActionItem(icon = Icons.Default.AddCard, label = "Add bill") { onNavigate("bills") }
                                QuickActionItem(icon = Icons.Default.PostAdd, label = "Secure Doc") { onNavigate("documents") }
                                QuickActionItem(icon = Icons.Default.Inbox, label = "AI Import") { onNavigate("inbox") }
                            }

                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Color(0xFFDC2626))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Emergency SOS Beacon", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text("Broadcast urgent care logs to family", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                                Switch(
                                    checked = isEmergencyMode,
                                    onCheckedChange = { onTriggerEmergency() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFDC2626),
                                        checkedTrackColor = Color(0xFFFEE2E2)
                                    )
                                )
                            }
                        }
                    }
                }

                // Household notifications sublist
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lately in Household",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                if (notifications.isEmpty()) {
                    item {
                        Text(
                            text = "No recent notifications or triggers.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                        )
                    }
                } else {
                    items(notifications.take(3)) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (log.type) {
                                            "Bill" -> Color(0xFFFEF3C7)
                                            "Medicine" -> Color(0xFFFEE2E2)
                                            "Task" -> Color(0xFFD1FAE5)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (log.type) {
                                    "Bill" -> Icons.Default.Receipt
                                    "Medicine" -> Icons.Default.MedicalServices
                                    "Task" -> Icons.Default.Assignment
                                    else -> Icons.Default.Notifications
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = when (log.type) {
                                        "Bill" -> Color(0xFFB45309)
                                        "Medicine" -> Color(0xFFB91C1C)
                                        "Task" -> Color(0xFF047857)
                                        else -> Color(0xFF475569)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1E293B))
                                Text(log.message, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            "Activities" -> {
                // Chronological Family Activity Feed
                item {
                    Text(
                        text = "Family Activity Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (activities.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🍃", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Household Activity Yet", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("Complete a task, pay a bill, or record a dose to observe live activities.", fontSize = 11.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(activities) { activity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Circle Avatar for member
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(activity.avatarColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activity.memberName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activity.memberName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )

                                    Text(
                                        text = getRelativeTime(activity.timestamp),
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Action Type pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (activity.actionType) {
                                                "Task Completion", "Task" -> Color(0xFFD1FAE5)
                                                "Bill Payment", "Bill" -> Color(0xFFFEF3C7)
                                                "Document Upload", "Document" -> Color(0xFFE0F2FE)
                                                "Expense Entry", "Expense" -> Color(0xFFF3E8FF)
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = activity.actionType.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (activity.actionType) {
                                            "Task Completion", "Task" -> Color(0xFF065F46)
                                            "Bill Payment", "Bill" -> Color(0xFF92400E)
                                            "Document Upload", "Document" -> Color(0xFF075985)
                                            "Expense Entry", "Expense" -> Color(0xFF6B21A8)
                                            else -> Color(0xFF475569)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = activity.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            "Smart AI" -> {
                // Biometrics Settings
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF2F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Biometric Bypass Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Enable fingerprint / face login",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = isBiometricSet,
                                    onCheckedChange = { onSetBiometricsEnabled(it) }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Secure storage of biometric credentials based on initial phone confirmation. Bypasses standard OTP logs for instant household access.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // AI Smart Reminder Suggestions
                if (suggestions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All Reminders are Optimised", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("Awesome! Our AI is continuously analyzing trends to propose health or task timing optimizations.", fontSize = 11.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "AI Smart Timing Suggestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(suggestions) { suggestion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFECFDF5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = suggestion.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Proposal for ${suggestion.targetType}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = suggestion.reason,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAF5), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Current Alarm", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        Text(suggestion.originalTiming, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8), style = TextStyle(textDecoration = TextDecoration.LineThrough))
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    Column(modifier = Modifier.weight(1.8f)) {
                                        Text("AI Recommended Change", fontSize = 10.sp, color = Color(0xFF047857))
                                        Text(suggestion.proposedAction, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onDismissSuggestion(suggestion.id) },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Dismiss", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Button(
                                        onClick = { onApproveSuggestion(suggestion.id) },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve Timing", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Companion AI" -> {
                // Inbuilt Age-Friendly Chat Assistant Room
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🤖", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Pilot Companion", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        Text("Any-Age Helpful offline AI", fontSize = 9.sp, color = Color(0xFF0369A1))
                                    }
                                }

                                IconButton(
                                    onClick = onClearChats,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset Chat", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

                            // Conversation Area (Height capped, scrollable)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .background(Color(0xFFF8FAF5), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chats.forEach { chat ->
                                    val isUser = chat.sender == "User"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isUser) 12.dp else 2.dp,
                                                        bottomEnd = if (isUser) 2.dp else 12.dp
                                                    )
                                                )
                                                .background(if (isUser) Color(0xFF059669) else Color(0xFFE2E8F0))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                                .widthIn(max = 220.dp)
                                        ) {
                                            Text(
                                                text = chat.message,
                                                fontSize = 11.sp,
                                                color = if (isUser) Color.White else Color(0xFF1E293B),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Prepopulated touches for elderly and children
                            Text("TAP QUICK QUESTIONS (ELDER/CHILDREN-FRIENDLY):", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val touchPrompts = listOf(
                                    "When is Dadi's medicine?",
                                    "Is school permit signed?",
                                    "Is our NetBroadband paid?"
                                )

                                touchPrompts.forEach { promptText ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFEEF2F6))
                                            .clickable { onSendChatMessage(promptText) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = promptText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Type input row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatMessageInput,
                                    onValueChange = { chatMessageInput = it },
                                    placeholder = { Text("Ask your pilot companion...", fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color(0xFFF8FAF5),
                                        focusedContainerColor = Color.White
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        if (chatMessageInput.isNotBlank()) {
                                            onSendChatMessage(chatMessageInput)
                                            chatMessageInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Relative timestamps function
fun getRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minute = 60 * 1000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "Just now"
        diff < 2 * minute -> "1 min ago"
        diff < hour -> "${diff / minute} mins ago"
        diff < 2 * hour -> "1 hour ago"
        diff < day -> "${diff / hour} hours ago"
        diff < 2 * day -> "Yesterday"
        else -> "${diff / day} days ago"
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
    }
}
