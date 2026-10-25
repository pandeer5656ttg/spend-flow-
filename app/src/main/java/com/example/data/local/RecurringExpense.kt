package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a recurring expense or subscription configuration.
 */
@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "amount")
  val amount: Double = 0.0,

  @ColumnInfo(name = "category")
  val category: String = "SUBSCRIPTIONS",

  @ColumnInfo(name = "date")
  val date: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "description")
  val description: String = "",

  @ColumnInfo(name = "recurrence_interval")
  val recurrenceInterval: String = "monthly", // "weekly", "monthly", "yearly"

  @ColumnInfo(name = "user_id")
  val userId: String = "",

  @ColumnInfo(name = "title")
  val title: String = "",

  @ColumnInfo(name = "frequency")
  val frequency: String = "monthly",

  @ColumnInfo(name = "billing_day")
  val billingDay: Int = 1,

  @ColumnInfo(name = "start_date")
  val startDate: Long = 0L,

  @ColumnInfo(name = "next_due_date")
  val nextDueDate: Long = 0L,

  @ColumnInfo(name = "last_processed_date")
  val lastProcessedDate: Long = 0L,

  @ColumnInfo(name = "is_active")
  val isActive: Boolean = true,

  @ColumnInfo(name = "icon_name")
  val iconName: String = "subscriptions"
) {
  val displayTitle: String get() = description.ifEmpty { title.ifEmpty { "Subscription" } }
  val displayInterval: String get() = recurrenceInterval.ifEmpty { frequency.ifEmpty { "monthly" } }
}
