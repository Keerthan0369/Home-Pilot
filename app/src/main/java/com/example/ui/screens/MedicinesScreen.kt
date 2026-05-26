package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FamilyMember
import com.example.data.entity.MedicineItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    medicines: List<MedicineItem>,
    familyMembers: List<FamilyMember>,
    onRecordDose: (medId: Int, isTaken: Boolean) -> Unit,
    onAddMedicine: (String, String, String, Int, Int, String) -> Unit,
    onDeleteMedicine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }

    val filteredMeds = if (selectedMemberId != null) {
        medicines.filter { it.memberId == selectedMemberId }
    } else {
        medicines
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // --- 1. Senior Friendly Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medicine Routine",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Elderly-friendly large trackers to avoid misses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Quick Member Filter Pills ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedMemberId == null,
                    onClick = { selectedMemberId = null },
                    label = { Text("All Family") }
                )
                
                familyMembers.take(4).forEach { member ->
                    FilterChip(
                        selected = selectedMemberId == member.id,
                        onClick = { selectedMemberId = member.id },
                        label = { Text(member.name.split(" ").first()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Routine List ---
            if (filteredMeds.isEmpty()) {
                EmptyStateView(
                    title = "Clear Prescription!",
                    message = "No active medical routines found. Add daily dose checklists securely.",
                    icon = Icons.Default.MedicalServices
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(filteredMeds) { med ->
                        val targetUser = familyMembers.find { it.id == med.memberId }
                        MedicineItemCard(
                            med = med,
                            user = targetUser,
                            onToggle = { onRecordDose(med.id, !med.isCheckedToday) },
                            onDelete = { onDeleteMedicine(med.id) }
                        )
                    }
                }
            }
        }

        // --- Add Medicine Dialog ---
        if (showAddDialog) {
            AddMedicineDialog(
                familyMembers = familyMembers,
                onDismiss = { showAddDialog = false },
                onAdd = { name, dosage, timing, duration, assignee, times ->
                    onAddMedicine(name, dosage, timing, duration, assignee, times)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MedicineItemCard(
    med: MedicineItem,
    user: FamilyMember?,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isTaken = med.isCheckedToday

    HomePilotCard(
        backgroundColor = if (isTaken) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
        elevation = if (isTaken) 0.dp else 1.5.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(
                                if (isTaken) Color(0xFF81C784) else Color(0xFFFFCDD2)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = if (isTaken) Color.White else Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = med.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${med.dosage} (${med.timing})",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Delete Icon Button
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.4f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Allocation to Family Member Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(user.avatarColor).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(user.avatarColor))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "For: ${user.name} (${user.role})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Elderly-friendly toggle checklist button - 48dp+ target
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTaken) Color(0xFF2E7D32) else Color(0xFFC62828)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .width(120.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTaken) "Taken" else "Take dose",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMedicineDialog(
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onAdd: (name: String, dosage: String, timing: String, durationDays: Int, memberId: Int, times: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var selectedTiming by remember { mutableStateOf("Morning (After food)") }
    var durationStr by remember { mutableStateOf("30") }
    var selectedMember by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: 1) }
    var times by remember { mutableStateOf("09:00") }

    val timings = listOf("Morning (After food)", "Morning (Before food)", "Afternoon (Lunch)", "Night (After dinner)", "As requested")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Medicine Routine", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name *") },
                        placeholder = { Text("e.g. Telmisartan (BP)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage *") },
                        placeholder = { Text("e.g. 1 Tablet, 10ml") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Select schedule timing:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    // Custom select option using basic horizontal buttons or spinner choice
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        timings.take(4).forEach { tim ->
                            val isSelected = selectedTiming == tim
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTiming = tim }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tim, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duration (Days)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Select recipient:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        familyMembers.forEach { member ->
                            val isActive = selectedMember == member.id
                            Button(
                                onClick = { selectedMember = member.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(member.name.split(" ").first(), fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = times,
                        onValueChange = { times = it },
                        label = { Text("Scheduled Reminder (HH:MM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = durationStr.toIntOrNull() ?: 30
                    if (name.isNotEmpty() && dosage.isNotEmpty()) {
                        onAdd(name, dosage, selectedTiming, days, selectedMember, times)
                    }
                },
                enabled = name.isNotEmpty() && dosage.isNotEmpty()
            ) {
                Text("Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
