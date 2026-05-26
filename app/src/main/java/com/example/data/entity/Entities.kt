package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // Admin, Co-admin, Adult, Child, Elderly, Caretaker, Read-only
    val phone: String,
    val avatarColor: Int, // Hex ARGB or Color Resource id
    val pin: String = "", // Optional PIN for biometrics simulation
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assigneeId: Int, // FamilyMember ID reference
    val dueDate: Long,
    val recurrence: String, // None, Daily, Weekly, Monthly, Yearly
    val priority: String, // Low, Medium, High
    val status: String, // Pending, In Progress, Completed, Overdue
    val attachmentsJson: String = "", // JSON list of local/simulated attachment URLs
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bills")
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Electricity, Water, Internet, Gas, Rent, Subscriptions, Insurance, Education, Other
    val amount: Double,
    val dueDate: Long,
    val status: String, // Unpaid, Paid, Overdue
    val notes: String = "",
    val isAutomated: Boolean = false,
    val paymentDate: Long? = null
)

@Entity(tableName = "medicines")
data class MedicineItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String, // e.g. "1 Pill", "5ml"
    val timing: String, // Morning, Afternoon, Evening, Night, Before Food, After Food
    val durationDays: Int,
    val memberId: Int, // FamilyMember ID reference
    val reminderTimes: String, // Comma separated e.g. "08:00, 20:00"
    val isActive: Boolean = true,
    val isCheckedToday: Boolean = false,
    val takenLogJson: String = "[]" // JSON representation of taken/missed logs
)

@Entity(tableName = "documents")
data class DocumentItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Aadhaar, PAN, Passport, Insurance, Vehicle, Certificates, Medical, Utility, Other
    val scannedText: String = "", // OCR extracted content
    val summary: String = "", // AI generated summary/extraction
    val expiryDate: Long? = null,
    val isBiometricProtected: Boolean = false,
    val uriString: String = "", // File pointer or base64/mock uri
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String, // Groceries, Fuel, Utilities, Rent, Education, Medical, Subscriptions, Shopping, Other
    val paidByMemberId: Int, // FamilyMember ID reference
    val splitWithJson: String = "[]", // JSON list of member IDs to split with
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "approvals")
data class ApprovalItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val details: String,
    val requesterName: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val itemType: String, // Document, Expense, CaretakerAccess, Override
    val itemId: Int = 0, // Reference to physical item
    val timestamp: Long = System.currentTimeMillis(),
    val decisionBy: String? = null,
    val decisionTime: Long? = null
)

@Entity(tableName = "family_inbox")
data class FamilyInboxItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val content: String,
    val extractedAction: String = "", // Structured JSON from AI extraction
    val isProcessed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // Task, Bill, Medicine, Approval, System
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "family_activities")
data class FamilyActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberName: String,
    val avatarColor: Int,
    val actionType: String, // Task, Bill, Document, Expense, Medicine, System
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_suggestions")
data class SmartSuggestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val originalTiming: String,
    val proposedAction: String,
    val reason: String,
    val targetType: String, // Medicine, Task
    val targetId: Int, // Ref to medicine or task id
    val isApproved: Boolean = false,
    val isDismissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "companion_chats")
data class CompanionChatLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "User" or "HomePilot AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
