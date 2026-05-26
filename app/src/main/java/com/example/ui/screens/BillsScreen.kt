package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BillItem
import com.example.ui.components.CategoryIcon
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    bills: List<BillItem>,
    onPayBill: (Int) -> Unit,
    onAddBill: (String, String, Double, Long, String, Boolean) -> Unit,
    onDeleteBill: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filterSelected by remember { mutableStateOf("All") } // All, Unpaid, Paid

    val filteredBills = bills.filter {
        when (filterSelected) {
            "Unpaid" -> it.status == "Unpaid" || it.status == "Overdue"
            "Paid" -> it.status == "Paid"
            else -> true
        }
    }

    val totalUnpaidSum = bills.filter { it.status == "Unpaid" || it.status == "Overdue" }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
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
            // --- 1. Main Header ---
            Text(
                text = "Bills Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Keep recurring household utilities organized",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Outstanding High Affinity Card ---
            HomePilotCard(backgroundColor = MaterialTheme.colorScheme.secondaryContainer) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Outstanding Amount Due",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%.2f".format(totalUnpaidSum)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Filters ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("All", "Unpaid", "Paid").forEach { item ->
                    val selected = filterSelected == item
                    FilterChip(
                        selected = selected,
                        onClick = { filterSelected = item },
                        label = { Text(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Bills list ---
            if (filteredBills.isEmpty()) {
                EmptyStateView(
                     title = "Clear Sheet!",
                     message = "No bills detected. Enjoy zero pending payments inside HomePilot.",
                     icon = Icons.Default.Check
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(filteredBills) { bill ->
                        BillItemCard(
                            bill = bill,
                            onPay = { onPayBill(bill.id) },
                            onDelete = { onDeleteBill(bill.id) }
                        )
                    }
                }
            }
        }

        // --- Add Bill Dialog ---
        if (showAddDialog) {
            AddBillDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, cat, amt, due, notes, auto ->
                    onAddBill(title, cat, amt, due, notes, auto)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BillItemCard(
    bill: BillItem,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = bill.status == "Paid"
    val isOverdue = bill.status == "Overdue" || (bill.dueDate < System.currentTimeMillis() && !isPaid)
    val df = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    HomePilotCard(
        backgroundColor = if (isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(category = bill.category)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Due before: ${df.format(Date(bill.dueDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
                if (bill.notes.isNotEmpty()) {
                    Text(
                        text = bill.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹${"%.0f".format(bill.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (!isPaid) {
                    Button(
                        onClick = onPay,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Pay", fontSize = 11.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1B6A4E), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Paid", fontSize = 11.sp, color = Color(0xFF1B6A4E), fontWeight = FontWeight.Bold)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, amount: Double, dueDate: Long, notes: String, auto: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Electricity") }
    var notes by remember { mutableStateOf("") }
    var isAutoPay by remember { mutableStateOf(false) }

    val categories = listOf("Electricity", "Water", "Internet", "Gas", "Rent", "Subscriptions", "Insurance", "Education", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Household Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Identifier *") },
                    placeholder = { Text("e.g. Tata Power Electricity") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Due (INR) *") },
                    placeholder = { Text("e.g. 4500") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category scroll list
                Text("Bill Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Let's do simple row chips for simplicity and compilation robust execution
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text(selectedCategory) }
                                )
                                // Render a small subset or list choice
                                Text("Choose type:", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }
                    }
                }
                
                // Render category options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.take(4).forEach { cat ->
                        val active = selectedCategory == cat
                        Button(
                            onClick = { selectedCategory = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(cat, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Consumer/Account No or details") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Auto-Pay Enabled", fontSize = 13.sp)
                    Switch(checked = isAutoPay, onCheckedChange = { isAutoPay = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0.0) {
                        onAdd(title, selectedCategory, amt, System.currentTimeMillis() + 7 * 86400000, notes, isAutoPay)
                    }
                },
                enabled = title.isNotEmpty() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Save Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
