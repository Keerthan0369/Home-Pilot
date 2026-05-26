package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.FamilyMember
import com.example.ui.screens.*
import com.example.ui.viewmodel.HomePilotViewModel
import com.example.ui.viewmodel.HomePilotViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePilotApp(
    viewModel: HomePilotViewModel,
    modifier: Modifier = Modifier
) {
    // --- ViewModel Flows state collecting ---
    val familyMembers by viewModel.familyMembers.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val approvals by viewModel.approvals.collectAsState()
    val inboxItems by viewModel.inboxItems.collectAsState()
    val notificationsLog by viewModel.notificationsLog.collectAsState()
    val activeMember by viewModel.currentMember.collectAsState()
    val isEmergencyMode by viewModel.isEmergencyMode.collectAsState()
    val isAiExtracting by viewModel.isAiExtracting.collectAsState()
    val isBiometricUnlocked by viewModel.biometricUnlocked.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val companionChats by viewModel.companionChats.collectAsState()
    val isBiometricSet by viewModel.isBiometricSet.collectAsState()

    // --- Local Navigation routing ---
    var currentRoute by remember { mutableStateOf("login") } // login, otp, dashboard, tasks, bills, medicines, documents, expenses, approvals, inbox, family
    var pendingPhoneInput by remember { mutableStateOf("") }
    
    // --- Profile dropdown switcher ---
    var showProfileDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute != "login" && currentRoute != "otp") {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HomePilot",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { currentRoute = "dashboard" }) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Home")
                        }
                    },
                    actions = {
                        // Quick Active user Profile Swapper indicator
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(activeMember?.avatarColor ?: 0xFF105844.toInt()))
                                .clickable { showProfileDropdown = !showProfileDropdown },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeMember?.name?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showProfileDropdown,
                            onDismissRequest = { showProfileDropdown = false }
                        ) {
                            Text(
                                "Choose Household view:",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            familyMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { 
                                        Text("${member.name} (${member.role})", fontSize = 13.sp) 
                                    },
                                    onClick = {
                                        viewModel.selectActiveProfile(member)
                                        showProfileDropdown = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFF8FAF5) // Blend seamlessly with high-profile brand background
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute != "login" && currentRoute != "otp") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface, // Crisp white card surface
                    tonalElevation = 0.dp // High-fidelity clean minimal style without heavy shadow overlay tints
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { currentRoute = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "tasks",
                        onClick = { currentRoute = "tasks" },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                        label = { Text("Tasks", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "documents",
                        onClick = { currentRoute = "documents" },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Vault") },
                        label = { Text("Vault", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "expenses",
                        onClick = { currentRoute = "expenses" },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Expenses") },
                        label = { Text("Expenses", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "family",
                        onClick = { currentRoute = "family" },
                        icon = { Icon(Icons.Default.People, contentDescription = "Family") },
                        label = { Text("Family", fontSize = 10.sp) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "login" -> {
                    LoginScreen(
                        isBiometricSet = isBiometricSet,
                        onBiometricSuccess = {
                            val savedPhone = viewModel.getBiometricPhone()
                            pendingPhoneInput = if (savedPhone.isNotEmpty()) savedPhone else "9876543210"
                            viewModel.selectActiveProfile(familyMembers.firstOrNull { it.phone == pendingPhoneInput } ?: familyMembers.firstOrNull() ?: FamilyMember(name="Family", phone="", role="", avatarColor = 0xFF9E9E9E.toInt()))
                            currentRoute = "dashboard"
                        },
                        onSendOtp = { phone ->
                            pendingPhoneInput = phone
                            currentRoute = "otp"
                        }
                    )
                }
                "otp" -> {
                    OtpVerificationScreen(
                        phone = pendingPhoneInput,
                        onVerifyOtp = { verified ->
                            if (verified) {
                                currentRoute = "dashboard"
                            }
                        },
                        onBack = { currentRoute = "login" }
                    )
                }
                "dashboard" -> {
                    DashboardScreen(
                        currentMember = activeMember,
                        familyMembers = familyMembers,
                        tasks = tasks,
                        bills = bills,
                        medicines = medicines,
                        approvals = approvals,
                        notifications = notificationsLog,
                        isEmergencyMode = isEmergencyMode,
                        onNavigate = { route -> currentRoute = route },
                        onTriggerEmergency = { viewModel.toggleEmergencyMode() },
                        activities = activities,
                        suggestions = suggestions,
                        chats = companionChats,
                        isBiometricSet = isBiometricSet,
                        onApproveSuggestion = { id -> viewModel.approveSuggestion(id) },
                        onDismissSuggestion = { id -> viewModel.dismissSuggestion(id) },
                        onSendChatMessage = { msg -> viewModel.sendCompanionChatMessage(msg) },
                        onClearChats = { viewModel.clearCompanionChats() },
                        onSetBiometricsEnabled = { enabled -> viewModel.setBiometricsEnabled(enabled, pendingPhoneInput) }
                    )
                }
                "tasks" -> {
                    TasksScreen(
                        tasks = tasks,
                        familyMembers = familyMembers,
                        onToggleStatus = { taskId -> viewModel.toggleTaskStatus(taskId) },
                        onAddTask = { title, desc, assignee, due, priority, recurrence ->
                            viewModel.addTask(title, desc, assignee, due, priority, recurrence)
                        },
                        onDeleteTask = { id -> viewModel.deleteTask(id) }
                    )
                }
                "bills" -> {
                    BillsScreen(
                        bills = bills,
                        onPayBill = { id -> viewModel.payBill(id) },
                        onAddBill = { title, cat, amt, due, notes, auto ->
                            viewModel.addBill(title, cat, amt, due, notes, auto)
                        },
                        onDeleteBill = { id -> viewModel.deleteBill(id) }
                    )
                }
                "medicines" -> {
                    MedicinesScreen(
                        medicines = medicines,
                        familyMembers = familyMembers,
                        onRecordDose = { medId, isTaken -> viewModel.recordDose(medId, isTaken) },
                        onAddMedicine = { name, dose, timing, dur, memberId, times ->
                            viewModel.addMedicine(name, dose, timing, dur, memberId, times)
                        },
                        onDeleteMedicine = { id -> viewModel.deleteMedicine(id) }
                    )
                }
                "documents" -> {
                    DocumentsScreen(
                        documents = documents,
                        isBiometricUnlocked = isBiometricUnlocked,
                        isAiExtracting = isAiExtracting,
                        onAddDocument = { title, cat, rawText, isProtected ->
                            viewModel.addDocument(title, cat, rawText, isProtected)
                        },
                        onDeleteDocument = { id -> viewModel.deleteDocument(id) },
                        onUnlockPIN = { pin -> viewModel.verifyBiometricPin(pin) },
                        onLockVault = { viewModel.lockSecureVault() }
                    )
                }
                "expenses" -> {
                    ExpensesScreen(
                        expenses = expenses,
                        familyMembers = familyMembers,
                        onAddExpense = { title, amt, cat, payerId, splits ->
                             viewModel.addExpense(title, amt, cat, payerId, splits)
                        },
                        onDeleteExpense = { id -> viewModel.deleteExpense(id) }
                    )
                }
                "approvals" -> {
                    ApprovalsScreen(
                        approvals = approvals,
                        currentUserRole = activeMember?.role ?: "Read-only",
                        onResolveApproval = { approvalId, approved ->
                             viewModel.processApproval(approvalId, approved)
                        }
                    )
                }
                "inbox" -> {
                    FamilyInboxScreen(
                        inboxItems = inboxItems,
                        isExtracting = isAiExtracting,
                        onAddInboxMessage = { sender, text ->
                            viewModel.submitMessageToInbox(sender, text)
                        },
                        onApplyAction = { id -> viewModel.applyInboxItemAction(id) },
                        onDismissItem = { id -> viewModel.dismissInboxItem(id) }
                    )
                }
                "family" -> {
                    SettingsAndFamilyScreen(
                        familyMembers = familyMembers,
                        activeMember = activeMember,
                        notifications = notificationsLog,
                        onSelectProfile = { m -> viewModel.selectActiveProfile(m) },
                        onAddFamilyMember = { name, phone, role ->
                            viewModel.addFamilyMember(name, phone, role)
                        },
                        onNavigate = { route -> currentRoute = route }
                    )
                }
            }
        }
    }
}
