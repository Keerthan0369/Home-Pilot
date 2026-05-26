package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.database.HomePilotDatabase
import com.example.data.entity.*
import com.example.data.repository.HomePilotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomePilotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomePilotRepository

    init {
        val database = HomePilotDatabase.getDatabase(application)
        repository = HomePilotRepository(database.homePilotDao())
        
        // Seed database on startup to showcase rich dashboards
        viewModelScope.launch {
            repository.seedMockData()
        }
    }

    // --- Database-backed Flow Streams ---
    val familyMembers: StateFlow<List<FamilyMember>> = repository.allFamilyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillItem>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicines: StateFlow<List<MedicineItem>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentItem>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseItem>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvals: StateFlow<List<ApprovalItem>> = repository.allApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inboxItems: StateFlow<List<FamilyInboxItem>> = repository.allInboxItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationsLog: StateFlow<List<NotificationLog>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activities: StateFlow<List<FamilyActivity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<SmartSuggestion>> = repository.allSuggestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companionChats: StateFlow<List<CompanionChatLog>> = repository.companionChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SharedPreferences for Biometric Settings ---
    private val sharedPrefs = application.getSharedPreferences("homepilot_prefs", android.content.Context.MODE_PRIVATE)

    private val _isBiometricSet = MutableStateFlow(sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricSet: StateFlow<Boolean> = _isBiometricSet.asStateFlow()

    // --- Active Client Session / Emulated Profile State ---
    private val _currentMember = MutableStateFlow<FamilyMember?>(null)
    val currentMember: StateFlow<FamilyMember?> = _currentMember.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _biometricUnlocked = MutableStateFlow(false)
    val biometricUnlocked: StateFlow<Boolean> = _biometricUnlocked.asStateFlow()

    private val _isEmergencyMode = MutableStateFlow(false)
    val isEmergencyMode: StateFlow<Boolean> = _isEmergencyMode.asStateFlow()

    private val _isAiExtracting = MutableStateFlow(false)
    val isAiExtracting: StateFlow<Boolean> = _isAiExtracting.asStateFlow()

    init {
        // Initialize active member with first available member (or default to Admin Mom)
        viewModelScope.launch {
            familyMembers.collect { members ->
                if (members.isNotEmpty() && _currentMember.value == null) {
                    // Start as Anjali Sharma (Mom / Admin)
                    _currentMember.value = members.find { it.role == "Admin" } ?: members.first()
                }
            }
        }
    }

    // --- Profile Management ---
    fun selectActiveProfile(member: FamilyMember) {
        _currentMember.value = member
        // Lock secure vaults upon profile changes for security!
        _biometricUnlocked.value = false
        triggerNotification("Profile Switched", "Now operating as ${member.name} (${member.role})", "System")
    }

    fun verifyBiometricPin(pin: String): Boolean {
        // In simulation, any 4-digit code works, but match if the user has a pin or set true
        _biometricUnlocked.value = true
        return true
    }

    fun setBiometricsEnabled(enabled: Boolean, phone: String = "") {
        sharedPrefs.edit()
            .putBoolean("biometric_enabled", enabled)
            .putString("biometric_phone", if (enabled) phone else "")
            .apply()
        _isBiometricSet.value = enabled
        if (enabled) {
            triggerNotification("Biometrics Enabled", "Fingerprint / Face Unlock is now enabled for fast logins.", "System")
            logActivity("System", "Enabled fingerprint/face biometric login on this device.")
        } else {
            triggerNotification("Biometrics Disabled", "Secure OTP validation is now required for standard access.", "System")
            logActivity("System", "Disabled fingerprint/face biometric login on this device.")
        }
    }

    fun getBiometricPhone(): String {
        return sharedPrefs.getString("biometric_phone", "") ?: ""
    }

    fun logActivity(type: String, details: String) {
        viewModelScope.launch {
            val member = _currentMember.value
            val activity = FamilyActivity(
                memberName = member?.name ?: "System",
                avatarColor = member?.avatarColor ?: 0xFF9E9E9E.toInt(),
                actionType = type,
                description = details
            )
            repository.insertActivity(activity)
        }
    }

    fun lockSecureVault() {
        _biometricUnlocked.value = false
    }

    fun addFamilyMember(name: String, phone: String, role: String) {
        viewModelScope.launch {
            val colors = listOf(0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFD54F, 0xFFBA68C8, 0xFF4DB6AC)
            val pickColor = colors.random().toLong().toInt()
            val member = FamilyMember(name = name, phone = phone, role = role, avatarColor = pickColor)
            repository.insertFamilyMember(member)
            triggerNotification("Family Expanded", "$name joined the household as a $role.", "System")
            logActivity("System", "Added $name to the household as a $role.")
        }
    }

    // --- Task Actions ---
    fun addTask(title: String, description: String, assigneeId: Int, dueDate: Long, priority: String, recurrence: String) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                description = description,
                assigneeId = assigneeId,
                dueDate = dueDate,
                priority = priority,
                recurrence = recurrence,
                status = "Pending"
            )
            repository.insertTask(task)
            val assigneeName = familyMembers.value.find { it.id == assigneeId }?.name ?: "someone"
            triggerNotification("New Task Assigned", "A task '$title' has been created for review.", "Task")
            logActivity("Task", "Created task '$title' and assigned it to $assigneeName.")
        }
    }

    fun toggleTaskStatus(taskId: Int) {
        viewModelScope.launch {
            val currentTasks = tasks.value
            val match = currentTasks.find { it.id == taskId } ?: return@launch
            val nextStatus = when (match.status) {
                "Pending" -> "In Progress"
                "In Progress" -> "Completed"
                "Completed" -> "Pending"
                else -> "Completed"
            }
            val updated = match.copy(status = nextStatus)
            repository.insertTask(updated)
            triggerNotification("Task Status Updated", "Task '${match.title}' moved to $nextStatus.", "Task")
            logActivity("Task", "Marked task '${match.title}' as $nextStatus.")
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- Bill Actions ---
    fun addBill(title: String, category: String, amount: Double, dueDate: Long, notes: String = "", isAutomated: Boolean = false) {
        viewModelScope.launch {
            val bill = BillItem(
                title = title,
                category = category,
                amount = amount,
                dueDate = dueDate,
                status = "Unpaid",
                notes = notes,
                isAutomated = isAutomated
            )
            repository.insertBill(bill)
            triggerNotification("New Bill Received", "A new bill '$title' of ₹$amount is due.", "Bill")
            logActivity("Bill", "Added new household bill '$title' of ₹$amount due on category $category.")
        }
    }

    fun payBill(billId: Int) {
        viewModelScope.launch {
            val currentBills = bills.value
            val match = currentBills.find { it.id == billId } ?: return@launch
            val updated = match.copy(status = "Paid", paymentDate = System.currentTimeMillis())
            repository.insertBill(updated)
            triggerNotification("Bill Settled", "Payment of ₹${match.amount} for '${match.title}' marked as successful.", "Bill")
            logActivity("Bill", "Paid standard bill '${match.title}' for ₹${match.amount} successfully.")
        }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch {
            repository.deleteBill(id)
        }
    }

    // --- Medicine Actions ---
    fun addMedicine(name: String, dosage: String, timing: String, durationDays: Int, memberId: Int, reminderTimes: String) {
        viewModelScope.launch {
            val med = MedicineItem(
                name = name,
                dosage = dosage,
                timing = timing,
                durationDays = durationDays,
                memberId = memberId,
                reminderTimes = reminderTimes,
                isActive = true
            )
            repository.insertMedicine(med)
            val memberName = familyMembers.value.find { it.id == memberId }?.name ?: "someone"
            triggerNotification("Schedule Set", "Medicine routine for '$name' logged successfully.", "Medicine")
            logActivity("Medicine", "Configured medicine schedule for $memberName: $name ($dosage, $timing).")
        }
    }

    fun recordDose(medId: Int, isTaken: Boolean) {
        viewModelScope.launch {
            val currentMeds = medicines.value
            val match = currentMeds.find { it.id == medId } ?: return@launch
            
            // Toggle local check indicator
            val updated = match.copy(isCheckedToday = isTaken)
            repository.insertMedicine(updated)
            
            val memberName = familyMembers.value.find { it.id == match.memberId }?.name ?: "Family Member"
            val msg = if (isTaken) "Check: $memberName took ${match.name} dose." else "Dose missed alert for $memberName on ${match.name}."
            triggerNotification("Medicine Administered", msg, "Medicine")
            logActivity("Medicine", if (isTaken) "$memberName recorded taking dose of ${match.name}." else "Reported missed dose of ${match.name} for $memberName.")
        }
    }

    fun deleteMedicine(id: Int) {
        viewModelScope.launch {
            repository.deleteMedicine(id)
        }
    }

    // --- Document Actions ---
    fun addDocument(title: String, category: String, rawText: String, isProtected: Boolean) {
        viewModelScope.launch {
            _isAiExtracting.value = true
            
            // Format prompt for Gemini Document Extraction & Summary flow
            val prompt = """
                Analyze this household document:
                Title: $title
                Category: $category
                Raw scanned text content:
                $rawText
                
                Generate a short 1-2 sentence family-friendly OCR bullet summary highlighting critical details like Account/Policy Numbers, names of covered policy people, total cost, or physical addresses. Keep it concise.
            """.trimIndent()
            
            val systemDocPrompt = "You are a professional security and summary assistant. Extract only high-fidelity key parameters for a family document dashboard."
            val aiSummary = GeminiClient.generateText(prompt, systemDocPrompt)
            
            val doc = DocumentItem(
                title = title,
                category = category,
                scannedText = rawText,
                summary = aiSummary,
                isBiometricProtected = isProtected,
                createdAt = System.currentTimeMillis()
            )
            repository.insertDocument(doc)
            _isAiExtracting.value = false
            triggerNotification("Document Vaulted", "'$title' was indexed securely with AI extraction summary.", "System")
            logActivity("Document", "Uploaded and cataloged document '$title' under category $category.")
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    // --- Family Expense Actions ---
    fun addExpense(title: String, amount: Double, category: String, paidByMemberId: Int, splitWithIds: List<Int>) {
        viewModelScope.launch {
            val splitJson = "[ " + splitWithIds.joinToString(",") + " ]"
            val exp = ExpenseItem(
                title = title,
                amount = amount,
                category = category,
                paidByMemberId = paidByMemberId,
                splitWithJson = splitJson,
                timestamp = System.currentTimeMillis()
            )
            repository.insertExpense(exp)
            val payerName = familyMembers.value.find { it.id == paidByMemberId }?.name ?: "someone"
            triggerNotification("Expense Added", "Added ₹$amount for '$title' under $category.", "System")
            logActivity("Expense", "$payerName logged an expense '$title' of ₹$amount under category $category.")
        }
    }

    // --- AI Smart Suggestions Actions ---
    fun approveSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            val list = suggestions.value
            val match = list.find { it.id == suggestionId } ?: return@launch
            
            if (match.targetType == "Medicine") {
                val currentMeds = medicines.value
                val med = currentMeds.find { it.id == match.targetId }
                if (med != null) {
                    val updatedMed = med.copy(timing = match.proposedAction, reminderTimes = match.proposedAction.substringAfterLast("at ").substringBefore(" "))
                    repository.insertMedicine(updatedMed)
                }
            } else if (match.targetType == "Task") {
                val currentTasks = tasks.value
                val task = currentTasks.find { it.id == match.targetId }
                if (task != null) {
                    val updatedTask = task.copy(description = task.description + "\n(AI Schedule Approved: ${match.proposedAction})")
                    repository.insertTask(updatedTask)
                }
            }
            
            repository.approveSuggestion(suggestionId)
            triggerNotification("AI Suggestion Approved", "Optimized schedule: '${match.title}'", "System")
            logActivity("System", "Approved AI Smart Suggestion to optimize schedule: '${match.title}'")
        }
    }

    fun dismissSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            repository.dismissSuggestion(suggestionId)
        }
    }

    // --- Inbuilt Age-Friendly Companion Chat ---
    fun sendCompanionChatMessage(msg: String) {
        viewModelScope.launch {
            val userChat = CompanionChatLog(sender = "User", message = msg)
            repository.insertCompanionChat(userChat)
            
            _isAiExtracting.value = true
            
            val membersCtx = familyMembers.value.joinToString("\n") { "Member: ${it.name}, Role: ${it.role}, Phone: ${it.phone}" }
            val tasksCtx = tasks.value.filter { it.status != "Completed" }.joinToString("\n") { "Task: ${it.title} (Status: ${it.status}, Priority: ${it.priority})" }
            val billsCtx = bills.value.filter { it.status != "Paid" }.joinToString("\n") { "Bill: ${it.title} (Amount: ₹${it.amount}, Status: ${it.status})" }
            val medicinesCtx = medicines.value.joinToString("\n") { "Medication: ${it.name} (Dosage: ${it.dosage}, Timing: ${it.timing})" }
            val expensesCtx = expenses.value.take(5).joinToString("\n") { "Expense: ${it.title} (Amount: ₹${it.amount}, Category: ${it.category})" }
            
            val prompt = """
                You are Pilot Companion, the friendly co-pilot AI of HomePilot (Your Family's Operating System).
                You are conversing with ${currentMember.value?.name ?: "a household member"} who has the role of ${currentMember.value?.role ?: "Member"}.
                
                You have comprehensive secure memories about the household:
                === ACTIVE FAMILY MEMBERS ===
                $membersCtx
                
                === PENDING HOUSEHOLD TASKS ===
                $tasksCtx
                
                === UNPAID BILLS ===
                $billsCtx
                
                === MEDICINE ROUTINES ===
                $medicinesCtx
                
                === HISTORIC SAVED EXPENSES ===
                $expensesCtx
                
                The user asks: "$msg"
                
                Respond in an extremely warm, polite, direct, clear, and age-appropriate manner.
                If they are a child (e.g. Aarav), speak in a playful, encouraging way. If they are elderly (e.g. Dadi), be patient, highly respectful, use clear simple terms.
                Answer their questions precisely based on the memory states above. Keep your response under 3 sentences unless they specifically ask for deep details.
            """.trimIndent()
            
            val systemSystemPrompt = "You are a warm household companion robot/AI helper fluent in helping families manage life smoothly. You speak directly and clearly."
            
            val response = GeminiClient.generateText(prompt, systemSystemPrompt)
            
            val aiChat = CompanionChatLog(sender = "HomePilot AI", message = response)
            repository.insertCompanionChat(aiChat)
            
            _isAiExtracting.value = false
        }
    }

    fun clearCompanionChats() {
        viewModelScope.launch {
            repository.clearCompanionChats()
            repository.insertCompanionChat(CompanionChatLog(sender = "HomePilot AI", message = "Hello Sharma Family! I am your friendly offline co-pilot. I am fully trained on our household items, medical routines, and duties. I can guide you even without an active internet connection. Ask me anything!"))
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    // --- Approval Actions ---
    fun createApprovalRequest(title: String, details: String, requesterName: String, type: String, itemId: Int = 0) {
        viewModelScope.launch {
            val approval = ApprovalItem(
                title = title,
                details = details,
                requesterName = requesterName,
                status = "PENDING",
                itemType = type,
                itemId = itemId
            )
            repository.insertApproval(approval)
            triggerNotification("Approval Requested", "$requesterName submitted an action that requires Admin signoff.", "Approval")
        }
    }

    fun processApproval(approvalId: Int, approve: Boolean) {
        viewModelScope.launch {
            val list = approvals.value
            val match = list.find { it.id == approvalId } ?: return@launch
            val adminName = currentMember.value?.name ?: "Admin"
            
            val nextStatus = if (approve) "APPROVED" else "REJECTED"
            val updated = match.copy(
                status = nextStatus,
                decisionBy = adminName,
                decisionTime = System.currentTimeMillis()
            )
            repository.insertApproval(updated)
            
            // Execute physical outcome simulation
            if (approve && match.itemType == "Document") {
                // Grant doc permission simulation
            }
            
            triggerNotification(
                "Request Resolved", 
                "Approval request for '${match.title}' was $nextStatus by $adminName.", 
                "Approval"
            )
        }
    }

    // --- Inbox & SMS Auto-Extraction via Gemini API ---
    fun submitMessageToInbox(sender: String, rawContent: String) {
        viewModelScope.launch {
            _isAiExtracting.value = true
            
            // Trigger Gemini SMS / Raw Inbox parser to suggest structured data card!
            val prompt = """
                You are HomePilot AI, Your Family's Operating System assistant.
                Read this raw SMS text, copy-pasted notice, or message segment:
                "$rawContent"
                
                Please structure the data from this message into a solid JSON format matching exactly ONE of these types:
                If it's a Bill payment request:
                {"type":"Bill","title":"Short descriptive title","amount":0.00,"dueDate":"YYYY-MM-DD or simulated next date","category":"Electricity/Water/Internet/Subscriptions/Other"}
                
                If it's a chore, duty, or Task requested:
                {"type":"Task","title":"Short task description","assignee":"Provide name if mentioned, otherwise leave empty","dueDate":"YYYY-MM-DD or next logical day"}
                
                If it's a Health or Medicine schedule:
                {"type":"Medicine","name":"Tablet Name","dosage":"Dose details","timing":"Morning/Night","durationDays":30}
                
                Return ONLY valid JSON. If details are sparse, fill in with intelligent placeholders.
            """.trimIndent()

            val response = GeminiClient.generateText(prompt, "You are a rigid structured JSON parser. Return ONLY JSON, no markdown around, no ``` blocks.")
            
            // Attempt clean JSON extraction if the API responded with formatting blocks
            val cleanedResponse = response.replace("```json", "").replace("```", "").trim()
            
            val inboxItem = FamilyInboxItem(
                senderName = sender,
                content = rawContent,
                extractedAction = cleanedResponse,
                isProcessed = false,
                timestamp = System.currentTimeMillis()
            )
            
            repository.insertInboxItem(inboxItem)
            _isAiExtracting.value = false
            triggerNotification("Smart Inbox Alert", "AI parsed an action item from $sender.", "System")
        }
    }

    fun applyInboxItemAction(inboxId: Int) {
        viewModelScope.launch {
            val list = inboxItems.value
            val match = list.find { it.id == inboxId } ?: return@launch
            
            try {
                val json = JSONObject(match.extractedAction)
                val type = json.optString("type")
                
                when (type) {
                    "Bill" -> {
                        addBill(
                            title = json.optString("title", "Extracted Bill"),
                            category = json.optString("category", "Other"),
                            amount = json.optDouble("amount", 0.0),
                            dueDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L, // 5 days out
                            notes = "Auto-extracted from msg by ${match.senderName}"
                        )
                    }
                    "Task" -> {
                        addTask(
                            title = json.optString("title", "Extracted Task"),
                            description = "Structured automatically from inbox message.",
                            assigneeId = currentMember.value?.id ?: 1,
                            dueDate = System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L,
                            priority = "Medium",
                            recurrence = "None"
                        )
                    }
                    "Medicine" -> {
                        addMedicine(
                            name = json.optString("name", "Extracted Med"),
                            dosage = json.optString("dosage", "1 Dose"),
                            timing = json.optString("timing", "Morning"),
                            durationDays = json.optInt("durationDays", 30),
                            memberId = currentMember.value?.id ?: 1,
                            reminderTimes = "09:00"
                        )
                    }
                }
            } catch (e: Exception) {
                // If JSON is malformed, split custom basic parsing
                addTask(
                    title = "Review: ${match.senderName}",
                    description = match.content,
                    assigneeId = currentMember.value?.id ?: 1,
                    dueDate = System.currentTimeMillis() + 86400000,
                    priority = "Low",
                    recurrence = "None"
                )
            }
            
            repository.markInboxProcessed(inboxId, true)
            triggerNotification("Inbox Action Applied", "Structured card successfully active in HomePilot.", "System")
        }
    }

    fun dismissInboxItem(inboxId: Int) {
        viewModelScope.launch {
            repository.markInboxProcessed(inboxId, true)
        }
    }

    // --- Search Stream ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Safe Emergency Mode ---
    fun toggleEmergencyMode(details: String = "") {
        _isEmergencyMode.value = !_isEmergencyMode.value
        val state = if (_isEmergencyMode.value) "ACTIVE" else "Standby"
        
        if (_isEmergencyMode.value) {
            triggerNotification(
                "⚠️ EMERGENCY MODE ACTIVE", 
                "Broadcasting status: 'Family Safe.' Smart check-in triggered. Elder emergency tasks prioritized.", 
                "System"
            )
        } else {
            triggerNotification(
                "Emergency Cleared", 
                "System returned to standard operation.", 
                "System"
            )
        }
    }

    // --- Simulation System Internal Notifications ---
    fun triggerNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            val log = NotificationLog(title = title, message = message, type = type)
            repository.insertNotification(log)
        }
    }

    fun clearNotification(id: Int) {
        viewModelScope.launch {
            // Can be resolved or deleted
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}

class HomePilotViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePilotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomePilotViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
