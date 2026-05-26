package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HomePilotDao {

    // --- Family Member ---
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllFamilyMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMember): Long

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteFamilyMember(id: Int)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Int)

    // --- Bills ---
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<BillItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillItem): Long

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: Int)

    // --- Medicines ---
    @Query("SELECT * FROM medicines")
    fun getAllMedicines(): Flow<List<MedicineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineItem): Long

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicine(id: Int)

    // --- Documents ---
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentItem): Long

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: Int)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    // --- Approvals ---
    @Query("SELECT * FROM approvals ORDER BY timestamp DESC")
    fun getAllApprovals(): Flow<List<ApprovalItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalItem): Long

    // --- Family Inbox ---
    @Query("SELECT * FROM family_inbox ORDER BY timestamp DESC")
    fun getAllInboxItems(): Flow<List<FamilyInboxItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxItem(item: FamilyInboxItem): Long

    @Query("UPDATE family_inbox SET isProcessed = :isProcessed WHERE id = :id")
    suspend fun markInboxProcessed(id: Int, isProcessed: Boolean)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- Family Activities ---
    @Query("SELECT * FROM family_activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<FamilyActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: FamilyActivity): Long

    @Query("DELETE FROM family_activities")
    suspend fun clearAllActivities()

    // --- Smart Suggestions ---
    @Query("SELECT * FROM smart_suggestions WHERE isApproved = 0 AND isDismissed = 0 ORDER BY timestamp DESC")
    fun getActiveSuggestions(): Flow<List<SmartSuggestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SmartSuggestion): Long

    @Query("UPDATE smart_suggestions SET isApproved = 1 WHERE id = :id")
    suspend fun approveSuggestion(id: Int)

    @Query("UPDATE smart_suggestions SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissSuggestion(id: Int)

    // --- Companion Chats ---
    @Query("SELECT * FROM companion_chats ORDER BY timestamp ASC")
    fun getCompanionChats(): Flow<List<CompanionChatLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanionChat(chat: CompanionChatLog): Long

    @Query("DELETE FROM companion_chats")
    suspend fun clearCompanionChats()
}
