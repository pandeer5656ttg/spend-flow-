package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.BudgetGoal
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.model.RecurringExpense
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.data.local.SpendFlowDatabase
import com.example.data.local.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "SpendFlowRepo"

class SpendFlowRepository(
  private val db: FirebaseFirestore,
  private val localDb: SpendFlowDatabase? = null
) {

  // Convenience constructor reading custom database ID from firebase_applet_config.xml
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    SpendFlowDatabase.getDatabase(context.applicationContext)
  )

  val localDatabase: SpendFlowDatabase? get() = localDb

  private val auth get() = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
  }

  // --- USER PROFILE & SUBSCRIPTIONS ---

  fun getUserProfileFlow(userId: String): Flow<UserProfile?> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(null)
      close()
      return@callbackFlow
    }
    val docRef = db.collection("users").document(userId)
    val listener = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.e(TAG, "Error fetching user profile", error)
        trySend(null)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        trySend(snapshot.toObject(UserProfile::class.java))
      } else {
        trySend(null)
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun saveUserProfile(profile: UserProfile) {
    val uid = requireUserId()
    val sanitized = profile.copy(userId = uid)
    db.collection("users").document(uid).set(sanitized).await()
  }

  suspend fun updateSubscriptionTier(tier: String, isPro: Boolean, expiryDate: Long = 0L) {
    val uid = requireUserId()
    db.collection("users").document(uid).update(
      mapOf(
        "subscriptionTier" to tier,
        "isPro" to isPro,
        "subscriptionExpiryDate" to expiryDate
      )
    ).await()
  }

  suspend fun updateMonthlyBudgetGoal(budgetGoal: Double) {
    val uid = requireUserId()
    db.collection("users").document(uid).update(
      mapOf("monthlyBudgetGoal" to budgetGoal)
    ).await()
  }

  // --- EXPENSES ---

  fun getExpensesFlow(userId: String): Flow<List<Expense>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val query = db.collection("users").document(userId)
      .collection("expenses")
      .orderBy("timestamp", Query.Direction.DESCENDING)

    val listener = query.addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.e(TAG, "Error fetching expenses", error)
        trySend(emptyList())
        return@addSnapshotListener
      }
      val list = snapshots?.toObjects(Expense::class.java) ?: emptyList()
      trySend(list)
      launch(Dispatchers.IO) {
        try {
          localDb?.expenseDao()?.insertExpenses(list.map { it.toEntity() })
        } catch (e: Exception) {
          Log.w(TAG, "Local Room cache insert error", e)
        }
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun addExpense(expense: Expense) {
    val uid = requireUserId()
    val id = if (expense.id.isEmpty()) UUID.randomUUID().toString() else expense.id
    val sanitized = expense.copy(id = id, userId = uid)
    try {
      localDb?.expenseDao()?.insertExpense(sanitized.toEntity())
    } catch (e: Exception) {
      Log.w(TAG, "Local Room write error", e)
    }
    db.collection("users").document(uid)
      .collection("expenses").document(id)
      .set(sanitized)
      .await()
  }

  suspend fun deleteExpense(expenseId: String) {
    val uid = requireUserId()
    try {
      localDb?.expenseDao()?.deleteExpenseById(expenseId)
    } catch (e: Exception) {
      Log.w(TAG, "Local Room delete error", e)
    }
    db.collection("users").document(uid)
      .collection("expenses").document(expenseId)
      .delete()
      .await()
  }

  // --- RECURRING EXPENSES ---

  fun getRecurringExpensesFlow(userId: String): Flow<List<RecurringExpense>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val query = db.collection("users").document(userId)
      .collection("recurring_expenses")
      .orderBy("billingDay", Query.Direction.ASCENDING)

    val listener = query.addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.e(TAG, "Error fetching recurring expenses", error)
        trySend(emptyList())
        return@addSnapshotListener
      }
      val list = snapshots?.toObjects(RecurringExpense::class.java) ?: emptyList()
      trySend(list)
      launch(Dispatchers.IO) {
        try {
          localDb?.recurringExpenseDao()?.insertRecurringExpenses(list.map { it.toEntity() })
        } catch (e: Exception) {
          Log.w(TAG, "Local Room recurring cache error", e)
        }
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun saveRecurringExpense(recurring: RecurringExpense) {
    val uid = requireUserId()
    val id = if (recurring.id.isEmpty()) UUID.randomUUID().toString() else recurring.id
    val sanitized = recurring.copy(id = id, userId = uid)
    try {
      localDb?.recurringExpenseDao()?.insertRecurringExpense(sanitized.toEntity())
    } catch (e: Exception) {
      Log.w(TAG, "Local Room recurring write error", e)
    }
    db.collection("users").document(uid)
      .collection("recurring_expenses").document(id)
      .set(sanitized)
      .await()
  }

  suspend fun deleteRecurringExpense(recurringId: String) {
    val uid = requireUserId()
    try {
      localDb?.recurringExpenseDao()?.deleteRecurringExpenseById(recurringId)
    } catch (e: Exception) {
      Log.w(TAG, "Local Room recurring delete error", e)
    }
    db.collection("users").document(uid)
      .collection("recurring_expenses").document(recurringId)
      .delete()
      .await()
  }

  // --- BUDGET GOALS ---

  fun getBudgetGoalsFlow(userId: String): Flow<List<BudgetGoal>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val query = db.collection("users").document(userId)
      .collection("budget_goals")

    val listener = query.addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.e(TAG, "Error fetching budget goals", error)
        trySend(emptyList())
        return@addSnapshotListener
      }
      val list = snapshots?.toObjects(BudgetGoal::class.java) ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  suspend fun saveBudgetGoal(goal: BudgetGoal) {
    val uid = requireUserId()
    val id = if (goal.id.isEmpty()) UUID.randomUUID().toString() else goal.id
    val sanitized = goal.copy(id = id, userId = uid)
    db.collection("users").document(uid)
      .collection("budget_goals").document(id)
      .set(sanitized)
      .await()
  }

  // --- SEED SAMPLE DATA ON FIRST LAUNCH ---

  suspend fun seedSampleDataIfEmpty(userId: String) {
    try {
      val expensesSnapshot = db.collection("users").document(userId)
        .collection("expenses")
        .limit(1)
        .get()
        .await()

      if (expensesSnapshot.isEmpty) {
        // Create initial User Profile
        val userProfile = UserProfile(
          userId = userId,
          email = auth.currentUser?.email ?: "user@spendflow.app",
          displayName = auth.currentUser?.displayName ?: "SpendFlow User",
          subscriptionTier = "free",
          isPro = false,
          monthlyIncome = 5200.0,
          monthlyBudgetGoal = 3200.0
        )
        db.collection("users").document(userId).set(userProfile).await()

        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // Default Recurring Expenses (Netflix, Spotify, Gym, Rent, Fiber Internet)
        val defaultRecurring = listOf(
          RecurringExpense(
            id = "rec_rent",
            userId = userId,
            title = "Apartment Rent",
            description = "Apartment Rent",
            amount = 1450.0,
            category = ExpenseCategory.HOUSING.name,
            frequency = "monthly",
            recurrenceInterval = "monthly",
            billingDay = 1,
            date = now,
            nextDueDate = RecurringExpenseEngine.calculateNextDueDate("monthly", 1, now),
            isActive = true
          ),
          RecurringExpense(
            id = "rec_netflix",
            userId = userId,
            title = "Netflix 4K Ultra",
            description = "Netflix 4K Ultra",
            amount = 22.99,
            category = ExpenseCategory.SUBSCRIPTIONS.name,
            frequency = "monthly",
            recurrenceInterval = "monthly",
            billingDay = 12,
            date = now,
            nextDueDate = RecurringExpenseEngine.calculateNextDueDate("monthly", 12, now),
            isActive = true
          ),
          RecurringExpense(
            id = "rec_gym",
            userId = userId,
            title = "Equinox Fitness",
            description = "Equinox Fitness",
            amount = 65.0,
            category = ExpenseCategory.HEALTHCARE.name,
            frequency = "monthly",
            recurrenceInterval = "monthly",
            billingDay = 15,
            date = now,
            nextDueDate = RecurringExpenseEngine.calculateNextDueDate("monthly", 15, now),
            isActive = true
          ),
          RecurringExpense(
            id = "rec_spotify",
            userId = userId,
            title = "Spotify Duo",
            description = "Spotify Duo",
            amount = 14.99,
            category = ExpenseCategory.SUBSCRIPTIONS.name,
            frequency = "monthly",
            recurrenceInterval = "monthly",
            billingDay = 22,
            date = now,
            nextDueDate = RecurringExpenseEngine.calculateNextDueDate("monthly", 22, now),
            isActive = true
          )
        )

        for (rec in defaultRecurring) {
          db.collection("users").document(userId)
            .collection("recurring_expenses").document(rec.id)
            .set(rec).await()
        }

        // Realistic seed expenses across the current month
        val sampleExpenses = listOf(
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Whole Foods Organic Groceries", description = "Whole Foods Organic Groceries", amount = 142.50, category = ExpenseCategory.FOOD.name, date = now - oneDay, timestamp = now - oneDay),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Uber Ride to Tech Meetup", description = "Uber Ride to Tech Meetup", amount = 28.40, category = ExpenseCategory.TRANSPORT.name, date = now - (oneDay * 2), timestamp = now - (oneDay * 2)),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Blue Bottle Coffee & Pastry", description = "Blue Bottle Coffee & Pastry", amount = 16.75, category = ExpenseCategory.FOOD.name, date = now - (oneDay * 3), timestamp = now - (oneDay * 3)),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Electric & Power Utility", description = "Electric & Power Utility", amount = 89.20, category = ExpenseCategory.UTILITIES.name, date = now - (oneDay * 4), timestamp = now - (oneDay * 4)),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Cinema IMAX Tickets", description = "Cinema IMAX Tickets", amount = 38.00, category = ExpenseCategory.ENTERTAINMENT.name, date = now - (oneDay * 6), timestamp = now - (oneDay * 6)),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Trader Joe's Pantry Run", description = "Trader Joe's Pantry Run", amount = 94.60, category = ExpenseCategory.FOOD.name, date = now - (oneDay * 7), timestamp = now - (oneDay * 7)),
          Expense(id = UUID.randomUUID().toString(), userId = userId, title = "Gas Station Fuel Refill", description = "Gas Station Fuel Refill", amount = 54.00, category = ExpenseCategory.TRANSPORT.name, date = now - (oneDay * 9), timestamp = now - (oneDay * 9))
        )

        for (exp in sampleExpenses) {
          db.collection("users").document(userId)
            .collection("expenses").document(exp.id)
            .set(exp).await()
        }

        // Default Budget Goals
        val budgetGoals = listOf(
          BudgetGoal(id = "bg_food", userId = userId, category = ExpenseCategory.FOOD.name, monthlyLimit = 650.0),
          BudgetGoal(id = "bg_housing", userId = userId, category = ExpenseCategory.HOUSING.name, monthlyLimit = 1600.0),
          BudgetGoal(id = "bg_transport", userId = userId, category = ExpenseCategory.TRANSPORT.name, monthlyLimit = 250.0),
          BudgetGoal(id = "bg_subs", userId = userId, category = ExpenseCategory.SUBSCRIPTIONS.name, monthlyLimit = 120.0),
          BudgetGoal(id = "bg_ent", userId = userId, category = ExpenseCategory.ENTERTAINMENT.name, monthlyLimit = 200.0)
        )

        for (bg in budgetGoals) {
          db.collection("users").document(userId)
            .collection("budget_goals").document(bg.id)
            .set(bg).await()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed seeding initial data", e)
    }
  }
}
