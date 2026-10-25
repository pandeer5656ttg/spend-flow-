package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

  @Query("SELECT * FROM recurring_expenses WHERE user_id = :userId ORDER BY billing_day ASC")
  fun getRecurringExpensesByUserId(userId: String): Flow<List<RecurringExpense>>

  @Query("SELECT * FROM recurring_expenses ORDER BY billing_day ASC")
  fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>

  @Query("SELECT * FROM recurring_expenses WHERE user_id = :userId AND is_active = 1")
  suspend fun getActiveRecurringExpenses(userId: String): List<RecurringExpense>

  @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
  suspend fun getRecurringExpenseById(id: String): RecurringExpense?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecurringExpense(recurring: RecurringExpense)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecurringExpenses(recurringList: List<RecurringExpense>)

  @Update
  suspend fun updateRecurringExpense(recurring: RecurringExpense)

  @Query("UPDATE recurring_expenses SET is_active = :isActive WHERE id = :id")
  suspend fun updateActiveStatus(id: String, isActive: Boolean)

  @Query("UPDATE recurring_expenses SET last_processed_date = :lastProcessedDate, next_due_date = :nextDueDate WHERE id = :id")
  suspend fun updateProcessedDates(id: String, lastProcessedDate: Long, nextDueDate: Long)

  @Query("DELETE FROM recurring_expenses WHERE id = :id")
  suspend fun deleteRecurringExpenseById(id: String)

  @Query("DELETE FROM recurring_expenses WHERE user_id = :userId")
  suspend fun deleteRecurringExpensesForUser(userId: String)
}
