package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class SubscriptionTier(val title: String, val priceDisplay: String, val billingPeriod: String) {
  FREE("Free Starter", "$0", "Forever free"),
  PRO_MONTHLY("Pro Monthly", "$4.99", "per month"),
  PRO_ANNUAL("Pro Annual", "$39.99", "per year ($3.33/mo)"),
  PRO_LIFETIME("Pro Lifetime", "$79.99", "one-time payment")
}

data class UserProfile(
  val userId: String = "",
  val email: String = "",
  val displayName: String = "Saver",
  val subscriptionTier: String = "free",
  val isPro: Boolean = false,
  val subscriptionExpiryDate: Long = 0L,
  val currency: String = "$",
  val monthlyIncome: Double = 4500.0,
  val monthlyBudgetGoal: Double = 3200.0,
  val createdAt: Long = System.currentTimeMillis()
) {
  fun getTierEnum(): SubscriptionTier {
    return when (subscriptionTier) {
      "pro_monthly" -> SubscriptionTier.PRO_MONTHLY
      "pro_annual" -> SubscriptionTier.PRO_ANNUAL
      "pro_lifetime" -> SubscriptionTier.PRO_LIFETIME
      else -> SubscriptionTier.FREE
    }
  }
}

enum class ExpenseCategory(
  val displayName: String,
  val iconName: String,
  val color: Color
) {
  FOOD("Food & Dining", "restaurant", CategoryFood),
  HOUSING("Housing & Rent", "home", CategoryHousing),
  TRANSPORT("Transportation", "directions_car", CategoryTransport),
  SUBSCRIPTIONS("Subscriptions & SaaS", "subscriptions", CategorySubscriptions),
  ENTERTAINMENT("Entertainment", "movie", CategoryEntertainment),
  UTILITIES("Utilities & Bills", "bolt", CategoryUtilities),
  HEALTHCARE("Healthcare & Fitness", "fitness_center", CategoryHealthcare),
  SHOPPING("Shopping & Retail", "shopping_bag", CategoryFood),
  OTHER("Miscellaneous", "category", CategoryOther);

  companion object {
    fun fromString(name: String): ExpenseCategory {
      return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
        ?: OTHER
    }
  }
}

typealias Expense = com.example.data.local.Expense

enum class RecurrenceFrequency(val displayName: String, val daysInterval: Int) {
  WEEKLY("Weekly", 7),
  MONTHLY("Monthly", 30),
  YEARLY("Yearly", 365)
}

typealias RecurringExpense = com.example.data.local.RecurringExpense

data class BudgetGoal(
  val id: String = "",
  val userId: String = "",
  val category: String = ExpenseCategory.FOOD.name,
  val monthlyLimit: Double = 500.0,
  val monthYear: String = "2026-10"
)

data class CategorySpend(
  val category: ExpenseCategory,
  val totalAmount: Double,
  val percentage: Float,
  val count: Int
)

data class DailySpend(
  val dayLabel: String,
  val amount: Double,
  val timestamp: Long
)
