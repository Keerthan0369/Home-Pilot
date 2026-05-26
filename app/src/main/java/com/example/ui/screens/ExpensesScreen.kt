package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseItem
import com.example.data.entity.FamilyMember
import com.example.ui.components.CategoryIcon
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard
import com.example.ui.components.SpendPieChart
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenses: List<ExpenseItem>,
    familyMembers: List<FamilyMember>,
    onAddExpense: (String, Double, String, Int, List<Int>) -> Unit,
    onDeleteExpense: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Aggregate spend categories
    val totalSpend = expenses.sumOf { it.amount }
    val grouped = expenses.groupBy { it.category }
    val categories = grouped.keys.toList()
    val values = grouped.values.map { items -> items.sumOf { it.amount } }
    
    // Modern colors for Spend Pie Chart
    val chartColors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFD54F),
        Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFFFB74D), Color(0xFF90A4AE)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
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
            // --- 1. Header Ledger ---
            Text(
                text = "Family Ledger",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Log household transactions, grocery slips & shares.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Spend Charts ---
            HomePilotCard(backgroundColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This Month's Spending Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${"%.2f".format(totalSpend)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SpendPieChart(
                        categories = categories,
                        values = values,
                        colors = chartColors,
                        modifier = Modifier.height(140.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- 3. Ledger lists ---
            if (expenses.isEmpty()) {
                EmptyStateView(
                    title = "Ledger Blank!",
                    message = "No monthly expenses logged yet. Tap Add Expense to log organic bills or school fees.",
                    icon = Icons.Default.AttachMoney
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(expenses) { exp ->
                        val payer = familyMembers.find { it.id == exp.paidByMemberId }
                        ExpenseItemCard(
                            expense = exp,
                            payer = payer,
                            onDelete = { onDeleteExpense(exp.id) }
                        )
                    }
                }
            }
        }

        // --- Add Expense dialog ---
        if (showAddDialog) {
            AddExpenseDialog(
                familyMembers = familyMembers,
                onDismiss = { showAddDialog = false },
                onAdd = { title, amt, cat, paidBy, splitWith ->
                    onAddExpense(title, amt, cat, paidBy, splitWith)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseItem,
    payer: FamilyMember?,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    HomePilotCard(backgroundColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(category = expense.category)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Paid by: ${payer?.name ?: "Family"} on ${df.format(Date(expense.timestamp))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹${"%.0f".format(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Split Equally",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, category: String, paidByMemberId: Int, splitWithIds: List<Int>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var selectedPayer by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: 1) }
    var selectedSplits by remember { mutableStateOf(familyMembers.map { it.id }) }

    val categories = listOf("Groceries", "Fuel", "Utilities", "Rent", "Education", "Medical", "Subscriptions", "Shopping")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Household Expense", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title *") },
                        placeholder = { Text("e.g. Daily local veggies") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount Spent (INR) *") },
                        placeholder = { Text("e.g. 150") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Category Tag:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(cat, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Text("Paid By Member:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        familyMembers.take(4).forEach { member ->
                            val active = selectedPayer == member.id
                            Button(
                                onClick = { selectedPayer = member.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(member.name.split(" ").first(), fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Text("Split equally with:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        familyMembers.forEach { member ->
                            val isChecked = selectedSplits.contains(member.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val next = selectedSplits.toMutableList()
                                        if (isChecked) next.remove(member.id) else next.add(member.id)
                                        selectedSplits = next
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        val next = selectedSplits.toMutableList()
                                        if (isChecked) next.remove(member.id) else next.add(member.id)
                                        selectedSplits = next
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(member.name, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0.0) {
                        onAdd(title, amt, selectedCategory, selectedPayer, selectedSplits)
                    }
                },
                enabled = title.isNotEmpty() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0 && selectedSplits.isNotEmpty()
            ) {
                Text("Settle Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
