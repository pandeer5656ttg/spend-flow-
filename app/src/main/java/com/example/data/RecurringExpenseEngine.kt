package com.example.data

import com.example.model.Expense
import com.example.model.RecurringExpense
import java.util.Calendar
import java.util.UUID

object RecurringExpenseEngine {

  data class ProcessingResult(
    val newExpenses: List<Expense>,
    val updatedRules: List<RecurringExpense>
  )

  /**
   * Processes all active recurring expenses. If a recurring expense is due
   * and hasn't been logged for the current period, automatically creates an Expense record.
   */
  fun evaluateAndLogRecurringExpenses(
    rules: List<RecurringExpense>,
    userId: String,
    currentTimeMs: Long = System.currentTimeMillis()
  ): ProcessingResult {
    val newExpenses = mutableListOf<Expense>()
    val updatedRules = mutableListOf<RecurringExpense>()

    val nowCal = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
    val currentYear = nowCal.get(Calendar.YEAR)
    val currentMonth = nowCal.get(Calendar.MONTH)
    val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)

    for (rule in rules) {
      if (!rule.isActive) continue

      var shouldLog = false
      var logTimestamp = currentTimeMs
      var nextDueMs = rule.nextDueDate

      when (rule.frequency.lowercase()) {
        "monthly" -> {
          // Construct target billing date for this month
          val billingCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            val maxDaysInMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            val validDay = rule.billingDay.coerceIn(1, maxDaysInMonth)
            set(Calendar.DAY_OF_MONTH, validDay)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }

          val billingDateMs = billingCal.timeInMillis

          // Check if billing date has passed in current month
          if (currentTimeMs >= billingDateMs) {
            // Check if lastProcessedDate is before this month's billing date
            val lastProcessedCal = Calendar.getInstance().apply {
              timeInMillis = if (rule.lastProcessedDate > 0) rule.lastProcessedDate else 0L
            }
            val alreadyProcessedThisMonth = rule.lastProcessedDate > 0 &&
                lastProcessedCal.get(Calendar.YEAR) == currentYear &&
                lastProcessedCal.get(Calendar.MONTH) == currentMonth

            if (!alreadyProcessedThisMonth) {
              shouldLog = true
              logTimestamp = billingDateMs
              // Next due date will be next month
              val nextMonthCal = (billingCal.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
              }
              nextDueMs = nextMonthCal.timeInMillis
            }
          } else {
            nextDueMs = billingDateMs
          }
        }
        "weekly" -> {
          val lastProcessed = rule.lastProcessedDate
          val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
          if (lastProcessed == 0L || currentTimeMs - lastProcessed >= sevenDaysMs) {
            shouldLog = true
            logTimestamp = currentTimeMs
            nextDueMs = currentTimeMs + sevenDaysMs
          }
        }
        "yearly" -> {
          val oneYearMs = 365L * 24 * 60 * 60 * 1000
          if (rule.lastProcessedDate == 0L || currentTimeMs - rule.lastProcessedDate >= oneYearMs) {
            shouldLog = true
            logTimestamp = currentTimeMs
            nextDueMs = currentTimeMs + oneYearMs
          }
        }
      }

      if (shouldLog) {
        val generatedExpense = Expense(
          id = UUID.randomUUID().toString(),
          userId = userId,
          title = "${rule.displayTitle} (Auto-logged)",
          description = "${rule.displayTitle} (Auto-logged)",
          amount = rule.amount,
          category = rule.category,
          date = logTimestamp,
          timestamp = logTimestamp,
          recurrenceInterval = rule.displayInterval,
          note = "Automated ${rule.displayInterval} subscription charge",
          isRecurring = true,
          recurringRuleId = rule.id
        )
        newExpenses.add(generatedExpense)

        val updatedRule = rule.copy(
          lastProcessedDate = logTimestamp,
          nextDueDate = nextDueMs
        )
        updatedRules.add(updatedRule)
      }
    }

    return ProcessingResult(newExpenses = newExpenses, updatedRules = updatedRules)
  }

  /**
   * Calculates the upcoming next due timestamp for display
   */
  fun calculateNextDueDate(frequency: String, billingDay: Int, currentTimeMs: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
    return when (frequency.lowercase()) {
      "monthly" -> {
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        if (currentDay > billingDay) {
          cal.add(Calendar.MONTH, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, billingDay.coerceIn(1, maxDays))
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.timeInMillis
      }
      "weekly" -> currentTimeMs + (7L * 24 * 60 * 60 * 1000)
      else -> currentTimeMs + (30L * 24 * 60 * 60 * 1000)
    }
  }
}
