package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing an individual expense transaction.
 */
@Entity(tableName = "expenses")
data class Expense(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "amount")
  val amount: Double = 0.0,

  @ColumnInfo(name = "category")
  val category: String = "OTHER",

  @ColumnInfo(name = "date")
  val date: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "description")
  val description: String = "",

  @ColumnInfo(name = "recurrence_interval")
  val recurrenceInterval: String = "",

  @ColumnInfo(name = "user_id")
  val userId: String = "",

  @ColumnInfo(name = "title")
  val title: String = "",

  @ColumnInfo(name = "timestamp")
  val timestamp: Long = 0L,

  @ColumnInfo(name = "note")
  val note: String = "",

  @ColumnInfo(name = "is_recurring")
  val isRecurring: Boolean = false,

  @ColumnInfo(name = "recurring_rule_id")
  val recurringRuleId: String = ""
) {
  val displayTitle: String get() = description.ifEmpty { title.ifEmpty { "Expense" } }
  val displayDate: Long get() = if (date > 0L) date else if (timestamp > 0L) timestamp else System.currentTimeMillis()
}
