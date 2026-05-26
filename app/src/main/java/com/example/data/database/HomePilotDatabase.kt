package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HomePilotDao
import com.example.data.entity.*

@Database(
    entities = [
        FamilyMember::class,
        TaskItem::class,
        BillItem::class,
        MedicineItem::class,
        DocumentItem::class,
        ExpenseItem::class,
        ApprovalItem::class,
        FamilyInboxItem::class,
        NotificationLog::class,
        FamilyActivity::class,
        SmartSuggestion::class,
        CompanionChatLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HomePilotDatabase : RoomDatabase() {
    abstract fun homePilotDao(): HomePilotDao

    companion object {
        @Volatile
        private var INSTANCE: HomePilotDatabase? = null

        fun getDatabase(context: Context): HomePilotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomePilotDatabase::class.java,
                    "homepilot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
