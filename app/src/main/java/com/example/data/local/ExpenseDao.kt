package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

  @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
  fun getExpensesByUserId(userId: String): Flow<List<Expense>>

  @Query("SELECT * FROM expenses ORDER BY date DESC")
  fun getAllExpenses(): Flow<List<Expense>>

  @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
  suspend fun getExpenseById(id: String): Expense?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: Expense)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(expenses: List<Expense>)

  @Update
  suspend fun updateExpense(expense: Expense)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: String)

  @Query("DELETE FROM expenses WHERE user_id = :userId")
  suspend fun deleteExpensesForUser(userId: String)

  @Query("SELECT COUNT(*) FROM expenses WHERE user_id = :userId")
  suspend fun getExpenseCount(userId: String): Int
}
