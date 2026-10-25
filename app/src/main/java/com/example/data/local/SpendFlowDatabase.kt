package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [Expense::class, RecurringExpense::class],
  version = 1,
  exportSchema = false
)
abstract class SpendFlowDatabase : RoomDatabase() {

  abstract fun expenseDao(): ExpenseDao
  abstract fun recurringExpenseDao(): RecurringExpenseDao

  companion object {
    @Volatile
    private var INSTANCE: SpendFlowDatabase? = null

    fun getDatabase(context: Context): SpendFlowDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SpendFlowDatabase::class.java,
          "spendflow_local.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
