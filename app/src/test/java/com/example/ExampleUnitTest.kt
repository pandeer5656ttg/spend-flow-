package com.example

import com.example.data.RecurringExpenseEngine
import com.example.model.RecurringExpense
import com.example.model.SubscriptionTier
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRecurringExpenseEngine_monthlyAutoLog() {
    val cal = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 15)
      set(Calendar.HOUR_OF_DAY, 12)
    }
    val now = cal.timeInMillis

    val recurringItem = RecurringExpense(
      id = "sub_netflix",
      userId = "user_123",
      title = "Netflix Premium",
      amount = 22.99,
      category = "SUBSCRIPTIONS",
      frequency = "monthly",
      billingDay = 10, // 10th already passed
      lastProcessedDate = 0L,
      isActive = true
    )

    val result = RecurringExpenseEngine.evaluateAndLogRecurringExpenses(
      rules = listOf(recurringItem),
      userId = "user_123",
      currentTimeMs = now
    )

    assertEquals(1, result.newExpenses.size)
    val generated = result.newExpenses.first()
    assertEquals("Netflix Premium (Auto-logged)", generated.title)
    assertEquals(22.99, generated.amount, 0.001)
    assertTrue(generated.isRecurring)

    assertEquals(1, result.updatedRules.size)
    assertTrue(result.updatedRules.first().lastProcessedDate > 0L)
  }

  @Test
  fun testSubscriptionTiers() {
    val annual = SubscriptionTier.PRO_ANNUAL
    assertEquals("$39.99", annual.priceDisplay)
    assertTrue(annual.title.contains("Pro Annual"))
  }

  @Test
  fun testRoomEntityConversions() {
    val expense = com.example.data.local.Expense(
      id = "exp_1",
      userId = "user_42",
      amount = 4.50,
      category = "FOOD",
      date = 1000L,
      description = "Latte Coffee",
      recurrenceInterval = "",
      title = "Latte Coffee",
      timestamp = 1000L,
      note = "Latte",
      isRecurring = false,
      recurringRuleId = ""
    )
    assertEquals(4.50, expense.amount, 0.001)
    assertEquals("FOOD", expense.category)
    assertEquals(1000L, expense.date)
    assertEquals("Latte Coffee", expense.description)

    val recurring = com.example.data.local.RecurringExpense(
      id = "rec_1",
      userId = "user_42",
      amount = 40.0,
      category = "HEALTHCARE",
      date = 2000L,
      description = "Monthly Gym Membership",
      recurrenceInterval = "monthly",
      title = "Monthly Gym Membership",
      frequency = "monthly",
      billingDay = 5
    )
    assertEquals(40.0, recurring.amount, 0.001)
    assertEquals("HEALTHCARE", recurring.category)
    assertEquals(2000L, recurring.date)
    assertEquals("Monthly Gym Membership", recurring.description)
    assertEquals("monthly", recurring.recurrenceInterval)
    assertEquals(5, recurring.billingDay)
  }

  @Test
  fun testExpenseViewModel_filterAndTotals() = kotlinx.coroutines.runBlocking {
    val items = mutableListOf<com.example.data.local.Expense>()
    val flow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.Expense>>(emptyList())

    val fakeDao = object : com.example.data.local.ExpenseDao {
      override fun getExpensesByUserId(userId: String) = flow
      override fun getAllExpenses() = flow
      override suspend fun getExpenseById(id: String) = items.firstOrNull { it.id == id }
      override suspend fun insertExpense(expense: com.example.data.local.Expense) {
        items.add(expense)
        flow.value = items.toList()
      }
      override suspend fun insertExpenses(expenses: List<com.example.data.local.Expense>) {
        items.addAll(expenses)
        flow.value = items.toList()
      }
      override suspend fun updateExpense(expense: com.example.data.local.Expense) {}
      override suspend fun deleteExpenseById(id: String) {
        items.removeAll { it.id == id }
        flow.value = items.toList()
      }
      override suspend fun deleteExpensesForUser(userId: String) {
        items.removeAll { it.userId == userId }
        flow.value = items.toList()
      }
      override suspend fun getExpenseCount(userId: String) = items.size
    }

    val viewModel = com.example.viewmodel.ExpenseViewModel(fakeDao)
    viewModel.insertExpense(
      amount = 15.0,
      category = "FOOD",
      description = "Lunch Burrito",
      userId = "user_1"
    )

    assertEquals("", viewModel.searchQuery.value)
    viewModel.setSearchQuery("Burrito")
    assertEquals("Burrito", viewModel.searchQuery.value)
  }

  @Test
  fun testExpenseWithCustomDate() = kotlinx.coroutines.runBlocking {
    val items = mutableListOf<com.example.data.local.Expense>()
    val fakeDao = object : com.example.data.local.ExpenseDao {
      val flow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.Expense>>(emptyList())
      override fun getExpensesByUserId(userId: String) = flow
      override fun getAllExpenses() = flow
      override suspend fun getExpenseById(id: String) = items.firstOrNull { it.id == id }
      override suspend fun insertExpense(expense: com.example.data.local.Expense) {
        items.add(expense)
        flow.value = items.toList()
      }
      override suspend fun insertExpenses(expenses: List<com.example.data.local.Expense>) {
        items.addAll(expenses)
      }
      override suspend fun updateExpense(expense: com.example.data.local.Expense) {}
      override suspend fun deleteExpenseById(id: String) { items.removeAll { it.id == id } }
      override suspend fun deleteExpensesForUser(userId: String) {}
      override suspend fun getExpenseCount(userId: String) = items.size
    }

    val specificDate = 1715000000000L
    val expense = com.example.data.local.Expense(
      id = "custom_exp_1",
      userId = "user_1",
      amount = 89.50,
      category = "UTILITIES",
      description = "Electric bill",
      date = specificDate,
      recurrenceInterval = "",
      title = "Electric bill",
      timestamp = specificDate,
      note = "Due 15th",
      isRecurring = false,
      recurringRuleId = ""
    )
    fakeDao.insertExpense(expense)

    assertEquals(1, items.size)
    val logged = items.first()
    assertEquals(89.50, logged.amount, 0.001)
    assertEquals("UTILITIES", logged.category)
    assertEquals(specificDate, logged.date)
    assertEquals(specificDate, logged.timestamp)
  }

  @Test
  fun testMonthlyBudgetGoal_calculations() {
    val profile = com.example.model.UserProfile(
      userId = "u1",
      monthlyIncome = 5000.0,
      monthlyBudgetGoal = 3200.0
    )
    assertEquals(3200.0, profile.monthlyBudgetGoal, 0.001)

    val totalSpent = 1250.0
    val remaining = profile.monthlyBudgetGoal - totalSpent
    val spentRatio = totalSpent / profile.monthlyBudgetGoal

    assertEquals(1950.0, remaining, 0.001)
    assertEquals(0.390625, spentRatio, 0.0001)
    assertTrue("Budget is healthy when spent under 70%", spentRatio < 0.70)
  }
}
