package com.example.data.repository

import com.example.data.dao.HomePilotDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take

class HomePilotRepository(private val dao: HomePilotDao) {

    val allFamilyMembers: Flow<List<FamilyMember>> = dao.getAllFamilyMembers()
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allBills: Flow<List<BillItem>> = dao.getAllBills()
    val allMedicines: Flow<List<MedicineItem>> = dao.getAllMedicines()
    val allDocuments: Flow<List<DocumentItem>> = dao.getAllDocuments()
    val allExpenses: Flow<List<ExpenseItem>> = dao.getAllExpenses()
    val allApprovals: Flow<List<ApprovalItem>> = dao.getAllApprovals()
    val allInboxItems: Flow<List<FamilyInboxItem>> = dao.getAllInboxItems()
    val allNotifications: Flow<List<NotificationLog>> = dao.getAllNotifications()
    val allActivities: Flow<List<FamilyActivity>> = dao.getAllActivities()
    val allSuggestions: Flow<List<SmartSuggestion>> = dao.getActiveSuggestions()
    val companionChats: Flow<List<CompanionChatLog>> = dao.getCompanionChats()

    // --- Create / Insert ---
    suspend fun insertFamilyMember(member: FamilyMember): Int {
        return dao.insertFamilyMember(member).toInt()
    }

    suspend fun deleteFamilyMember(id: Int) {
        dao.deleteFamilyMember(id)
    }

    suspend fun insertTask(task: TaskItem): Int {
        return dao.insertTask(task).toInt()
    }

    suspend fun deleteTask(id: Int) {
        dao.deleteTask(id)
    }

    suspend fun insertBill(bill: BillItem): Int {
        return dao.insertBill(bill).toInt()
    }

    suspend fun deleteBill(id: Int) {
        dao.deleteBill(id)
    }

    suspend fun insertMedicine(medicine: MedicineItem): Int {
        return dao.insertMedicine(medicine).toInt()
    }

    suspend fun deleteMedicine(id: Int) {
        dao.deleteMedicine(id)
    }

    suspend fun insertDocument(document: DocumentItem): Int {
        return dao.insertDocument(document).toInt()
    }

    suspend fun deleteDocument(id: Int) {
        dao.deleteDocument(id)
    }

    suspend fun insertExpense(expense: ExpenseItem): Int {
        return dao.insertExpense(expense).toInt()
    }

    suspend fun deleteExpense(id: Int) {
        dao.deleteExpense(id)
    }

    suspend fun insertApproval(approval: ApprovalItem): Int {
        return dao.insertApproval(approval).toInt()
    }

    suspend fun insertInboxItem(item: FamilyInboxItem): Int {
        return dao.insertInboxItem(item).toInt()
    }

    suspend fun markInboxProcessed(id: Int, isProcessed: Boolean) {
        dao.markInboxProcessed(id, isProcessed)
    }

    suspend fun insertNotification(notification: NotificationLog): Int {
        return dao.insertNotification(notification).toInt()
    }

    suspend fun markNotificationAsRead(id: Int) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    // --- Family Activities CRUD ---
    suspend fun insertActivity(activity: FamilyActivity): Int {
        return dao.insertActivity(activity).toInt()
    }

    suspend fun clearAllActivities() {
        dao.clearAllActivities()
    }

    // --- Smart Suggestions CRUD ---
    suspend fun insertSuggestion(suggestion: SmartSuggestion): Int {
        return dao.insertSuggestion(suggestion).toInt()
    }

    suspend fun approveSuggestion(id: Int) {
        dao.approveSuggestion(id)
    }

    suspend fun dismissSuggestion(id: Int) {
        dao.dismissSuggestion(id)
    }

    // --- Companion Chats CRUD ---
    suspend fun insertCompanionChat(chat: CompanionChatLog): Int {
        return dao.insertCompanionChat(chat).toInt()
    }

    suspend fun clearCompanionChats() {
        dao.clearCompanionChats()
    }

    // --- Data Seeding / Setup Mock Household OS ---
    suspend fun seedMockData(force: Boolean = false) {
        val existing = allFamilyMembers.take(1).first()
        if (existing.isNotEmpty() && !force) return // Already seeded
        
        // Seed Members
        val momId = dao.insertFamilyMember(FamilyMember(name = "Anjali Sharma", role = "Admin", phone = "+91 98765 43210", avatarColor = 0xFFE57373.toInt())).toInt()
        val dadId = dao.insertFamilyMember(FamilyMember(name = "Rajesh Sharma", role = "Co-admin", phone = "+91 98765 43211", avatarColor = 0xFF64B5F6.toInt())).toInt()
        val kidId = dao.insertFamilyMember(FamilyMember(name = "Aarav Sharma", role = "Child", phone = "+91 98765 43212", avatarColor = 0xFF81C784.toInt())).toInt()
        val gMomId = dao.insertFamilyMember(FamilyMember(name = "Dadi ji", role = "Elderly", phone = "+91 98765 43213", avatarColor = 0xFFFFD54F.toInt())).toInt()
        val caretakerId = dao.insertFamilyMember(FamilyMember(name = "Ramu Kaka", role = "Caretaker", phone = "+91 98765 43214", avatarColor = 0xFFBA68C8.toInt())).toInt()

        // Seed Tasks
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val task1Id = dao.insertTask(TaskItem(title = "Renew Car Insurance", description = "Call insurer and renew before the 30th to avoid penalty.", assigneeId = dadId, dueDate = now + 4 * dayMs, recurrence = "Yearly", priority = "High", status = "Pending")).toInt()
        val task2Id = dao.insertTask(TaskItem(title = "Buy Groceries", description = "Milk, organic eggs, avocados, fresh vegetables, soap, dishwasher tablets.", assigneeId = momId, dueDate = now + dayMs / 2, recurrence = "None", priority = "Medium", status = "In Progress")).toInt()
        val task3Id = dao.insertTask(TaskItem(title = "Fix water purifier leaks", description = "Water filter in kitchen dripping. Plumber contact: +91 99000 11223", assigneeId = caretakerId, dueDate = now + 2 * dayMs, recurrence = "None", priority = "High", status = "Pending")).toInt()
        val task4Id = dao.insertTask(TaskItem(title = "Science Project Submission", description = "Aarav needs science project file checked & signed.", assigneeId = dadId, dueDate = now - dayMs, recurrence = "None", priority = "Medium", status = "Overdue")).toInt()

        // Seed Bills
        dao.insertBill(BillItem(title = "Tata Power Electricity Bill", category = "Electricity", amount = 4250.00, dueDate = now + 7 * dayMs, status = "Unpaid", notes = "Consumer No: 028837162. Usually around Rs 4k-5k."))
        dao.insertBill(BillItem(title = "Airtel Fiber Internet", category = "Internet", amount = 1179.00, dueDate = now + 3 * dayMs, status = "Unpaid", notes = "Auto-debit scheduled but double check limit.", isAutomated = true))
        dao.insertBill(BillItem(title = "Society Maintenance Charges", category = "Rent", amount = 6500.00, dueDate = now - dayMs * 3, status = "Overdue", notes = "Payable to Royal Orchid CHS Account."))
        dao.insertBill(BillItem(title = "Netflix Subscription", category = "Subscriptions", amount = 649.00, dueDate = now + 12 * dayMs, status = "Paid", notes = "Premium 4K ultra HD plan.", paymentDate = now - dayMs))

        // Seed Medicines
        val med1Id = dao.insertMedicine(MedicineItem(name = "Telmisartan (B.P.)", dosage = "40 mg pill", timing = "Morning (After breakfast)", durationDays = 30, memberId = gMomId, reminderTimes = "08:30", isActive = true)).toInt()
        val med2Id = dao.insertMedicine(MedicineItem(name = "Omega-3 Salmon Oil", dosage = "1 Capsule", timing = "Night (After food)", durationDays = 60, memberId = dadId, reminderTimes = "21:15", isActive = true)).toInt()
        val med3Id = dao.insertMedicine(MedicineItem(name = "Multivitamin Active", dosage = "1 tablet", timing = "Afternoon (Lunch)", durationDays = 15, memberId = momId, reminderTimes = "13:30", isActive = true)).toInt()

        // Seed Documents
        val doc1Id = dao.insertDocument(DocumentItem(title = "Sharma Family Health Policy", category = "Insurance", scannedText = "HDFC ERGO Optima Super Secure. Policy No: 2816-1928-112. Sum Insured: INR 10,000,000. Coverage Includes: Rajesh, Anjali, Aarav, Dadi. Excludes pre-existing out-patient care in first 2 years.", summary = "HDFC ERGO Health Policy for Rajesh, Anjali, Aarav, and Dadi with ₹10L coverage. Policy Code: 2816-1928-112.", expiryDate = now + 300 * dayMs)).toInt()
        val doc2Id = dao.insertDocument(DocumentItem(title = "Dadi Passport Front & Back", category = "Passport", scannedText = "REPUBLIC OF INDIA PASSPORT. Document No: Z2819283. Surname: SHARMA, Given Names: SAVITRI DEVI, Nationality: INDIAN. Date of Birth: 12/04/1954. Date of Expiry: 15/09/2029.", summary = "Indian Passport of Savatri Devi Sharma (Dadi ji). Document Z2819283, Expiry: Sept 15, 2029.", expiryDate = now + 1200 * dayMs, isBiometricProtected = true)).toInt()
        val doc3Id = dao.insertDocument(DocumentItem(title = "Apartment Rent Agreement 2026", category = "Other", scannedText = "RENT AGREEMENT executed at Mumbai. Landlord: Suresh Mehta, Tenant: Rajesh Sharma. Property: Flat 501, Wing C, Orchid Heights. Rent: Rs. 65,000 per month. Security Deposit: Rs. 200,000. Tenure: 11 Months commencing 1st Jan 2026.", summary = "Rent Agreement: Flat 501, Orchid Heights between Rajesh Sharma (Tenant) & Suresh Mehta. Rent: ₹65k/m, Deposit: ₹2L.", expiryDate = now + 180 * dayMs)).toInt()

        // Seed Expenses
        dao.insertExpense(ExpenseItem(title = "Weekly Organic Groceries (D-Mart)", amount = 3850.00, category = "Groceries", paidByMemberId = momId, splitWithJson = "[$momId, $dadId]"))
        dao.insertExpense(ExpenseItem(title = "Car Fuel Full tank SUV", amount = 4200.00, category = "Fuel", paidByMemberId = dadId, splitWithJson = "[$momId, $dadId]"))
        dao.insertExpense(ExpenseItem(title = "Aarav Cricket Coaching Fees", amount = 3000.00, category = "Education", paidByMemberId = dadId, splitWithJson = "[$momId, $dadId]"))
        dao.insertExpense(ExpenseItem(title = "Dadi BP Monitor Purchase", amount = 1650.00, category = "Medical", paidByMemberId = momId, splitWithJson = "[$momId, $dadId]"))

        // Seed Approvals
        dao.insertApproval(ApprovalItem(title = "View Dad's Passport Document", details = "Aarav Sharma requested access to view 'Dad's Indian Passport' for school visa application.", requesterName = "Aarav Sharma", status = "PENDING", itemType = "Document", itemId = 2))
        dao.insertApproval(ApprovalItem(title = "Caretaker ramu_kaka Medical Access Override", details = "Ramu Kaka requested permission to view Dadi's medical folders to update pharmacy reports.", requesterName = "Ramu Kaka", status = "PENDING", itemType = "CaretakerAccess", itemId = 0))
        dao.insertApproval(ApprovalItem(title = "Reimburse Car Wash Expense 450 INR", details = "Caretaker Ramu Kaka claimed cash wash refund under miscellaneous expenses.", requesterName = "Ramu Kaka", status = "APPROVED", itemType = "Expense", itemId = 4, timestamp = now - dayMs, decisionBy = "Anjali Sharma", decisionTime = now - dayMs + 3600000))

        // Seed Family Activities Feed
        dao.insertActivity(FamilyActivity(memberName = "Anjali Sharma", avatarColor = 0xFFE57373.toInt(), actionType = "Task Completion", description = "Marked 'Sign Aarav picnic permit & send INR 850' chore as Completed."))
        dao.insertActivity(FamilyActivity(memberName = "Rajesh Sharma", avatarColor = 0xFF64B5F6.toInt(), actionType = "Bill Payment", description = "Paid Society Maintenance Charges of ₹6500 successfully via NetBanking."))
        dao.insertActivity(FamilyActivity(memberName = "Anjali Sharma", avatarColor = 0xFFE57373.toInt(), actionType = "Document Upload", description = "Uploaded 'Sharma Family Health Policy' HDFC Ergo insurance file."))
        dao.insertActivity(FamilyActivity(memberName = "Rajesh Sharma", avatarColor = 0xFF64B5F6.toInt(), actionType = "Expense Entry", description = "Added fuel refill expense of ₹4200 for the family SUV."))
        dao.insertActivity(FamilyActivity(memberName = "Aarav Sharma", avatarColor = 0xFF81C784.toInt(), actionType = "Medicine Dose", description = "Reported Savitri Devi (Dadi ji) successfully took Telmisartan BP medicine."))

        // Seed Smart AI Suggestions
        dao.insertSuggestion(SmartSuggestion(
            title = "Optimise BP schedule for Savitri Devi",
            originalTiming = "08:30 AM",
            proposedAction = "Move alarm to 06:15 AM (30m after early wake-up)",
            reason = "Dadi ji consistently wakes up at 05:45 AM. Moving BP medication closer to waking hours helps control early-morning blood pressure spikes more effectively.",
            targetType = "Medicine",
            targetId = med1Id
        ))
        dao.insertSuggestion(SmartSuggestion(
            title = "Optimise Omega-3 schedule for Rajesh Sharma",
            originalTiming = "09:15 PM (Dinner: 09:00 PM)",
            proposedAction = "Move to 09:30 PM (30m after typical dinner)",
            reason = "Omega-3 fish oils have significantly higher absorption when consumed with meals. Moving the alarm closer to the typical dinner slot ensures better gastric tolerance and bioavailability.",
            targetType = "Medicine",
            targetId = med2Id
        ))
        dao.insertSuggestion(SmartSuggestion(
            title = "Optimise Car Insurance Task due time",
            originalTiming = "Anytime",
            proposedAction = "Set due time to Saturday 10:30 AM",
            reason = "Dad's work schedule indicates high cognitive load during weekday hours. Rescheduling to a relaxed Saturday morning slot ensures proper assessment of policy documents without work disruptions.",
            targetType = "Task",
            targetId = task1Id
        ))

        // Seed Companion Chats Memory
        dao.insertCompanionChat(CompanionChatLog(sender = "HomePilot AI", message = "Hello Sharma Family! I am your friendly offline co-pilot. I am fully trained on our household items, medical routines, and duties. I can guide you even without an active internet connection. Ask me anything!"))
    }
}
