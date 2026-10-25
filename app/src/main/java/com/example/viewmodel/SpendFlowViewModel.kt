package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RecurringExpenseEngine
import com.example.data.SpendFlowRepository
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface AuthState {
  data object Loading : AuthState
  data object Unauthenticated : AuthState
  data class Authenticated(val userId: String, val email: String?, val displayName: String?) : AuthState
}

class SpendFlowViewModel(
  private val repository: SpendFlowRepository
) : ViewModel() {

  private val auth: FirebaseAuth = Firebase.auth

  private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _activeUserId = MutableStateFlow<String?>(null)

  private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val user = firebaseAuth.currentUser
    if (user != null) {
      _activeUserId.value = user.uid
      _authState.value = AuthState.Authenticated(
        userId = user.uid,
        email = user.email,
        displayName = user.displayName ?: user.email?.substringBefore("@")
      )
      // Seed default sample transactions on first sign-in
      viewModelScope.launch {
        repository.seedSampleDataIfEmpty(user.uid)
        runAutoRecurringCheck(user.uid)
      }
    } else {
      _activeUserId.value = null
      _authState.value = AuthState.Unauthenticated
    }
  }

  init {
    auth.addAuthStateListener(authListener)
  }

  override fun onCleared() {
    super.onCleared()
    auth.removeAuthStateListener(authListener)
  }

  // Reactive flows scoped to active user
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val userProfile: StateFlow<UserProfile?> = _activeUserId
    .flatMapLatest { uid ->
      if (uid == null) flowOf(null) else repository.getUserProfileFlow(uid)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<Expense>> = _activeUserId
    .flatMapLatest { uid ->
      if (uid == null) flowOf(emptyList()) else repository.getExpensesFlow(uid)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val recurringExpenses: StateFlow<List<RecurringExpense>> = _activeUserId
    .flatMapLatest { uid ->
      if (uid == null) flowOf(emptyList()) else repository.getRecurringExpensesFlow(uid)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val budgetGoals: StateFlow<List<BudgetGoal>> = _activeUserId
    .flatMapLatest { uid ->
      if (uid == null) flowOf(emptyList()) else repository.getBudgetGoalsFlow(uid)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _overrideMonthlyBudgetGoal = MutableStateFlow<Double?>(null)

  val monthlyBudgetGoal: StateFlow<Double> = combine(userProfile, _overrideMonthlyBudgetGoal) { profile, override ->
    override ?: profile?.monthlyBudgetGoal?.takeIf { it > 0.0 } ?: 3200.0
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3200.0)

  // Computed Derived Analytics
  val totalSpent: StateFlow<Double> = expenses.map { list ->
    list.sumOf { it.amount }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val categorySpends: StateFlow<List<CategorySpend>> = expenses.map { list ->
    val total = list.sumOf { it.amount }.coerceAtLeast(0.01)
    val grouped = list.groupBy { it.category }
    grouped.map { (catStr, items) ->
      val sum = items.sumOf { it.amount }
      CategorySpend(
        category = ExpenseCategory.fromString(catStr),
        totalAmount = sum,
        percentage = ((sum / total) * 100f).toFloat(),
        count = items.size
      )
    }.sortedByDescending { it.totalAmount }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val dailySpends: StateFlow<List<DailySpend>> = expenses.map { list ->
    val now = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val result = mutableListOf<DailySpend>()

    // Last 7 days
    for (i in 6 downTo 0) {
      val dayCal = (now.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -i)
      }
      val startOfDay = (dayCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis

      val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

      val dayAmount = list.filter { it.timestamp in startOfDay until endOfDay }
        .sumOf { it.amount }

      result.add(
        DailySpend(
          dayLabel = dayFormat.format(dayCal.time),
          amount = dayAmount,
          timestamp = startOfDay
        )
      )
    }
    result
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val isPro: StateFlow<Boolean> = userProfile.map { it?.isPro ?: false }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Automated monthly recurring expense check & execution
  fun triggerAutoRecurringCheck() {
    val uid = _activeUserId.value ?: return
    viewModelScope.launch {
      runAutoRecurringCheck(uid)
    }
  }

  private suspend fun runAutoRecurringCheck(userId: String) {
    val currentRules = recurringExpenses.value
    if (currentRules.isEmpty()) return

    val result = RecurringExpenseEngine.evaluateAndLogRecurringExpenses(currentRules, userId)
    for (newExp in result.newExpenses) {
      repository.addExpense(newExp)
    }
    for (updatedRule in result.updatedRules) {
      repository.saveRecurringExpense(updatedRule)
    }
  }

  // Expense CRUD
  fun addExpense(
    title: String,
    amount: Double,
    category: ExpenseCategory,
    note: String = "",
    date: Long = System.currentTimeMillis()
  ) {
    val uid = _activeUserId.value ?: return
    viewModelScope.launch {
      val expense = Expense(
        id = UUID.randomUUID().toString(),
        userId = uid,
        title = title.trim(),
        description = title.trim(),
        amount = amount,
        category = category.name,
        date = date,
        timestamp = date,
        note = note.trim()
      )
      repository.addExpense(expense)
    }
  }

  fun deleteExpense(expenseId: String) {
    viewModelScope.launch {
      repository.deleteExpense(expenseId)
    }
  }

  // Recurring Subscriptions CRUD
  fun addRecurringExpense(
    title: String,
    amount: Double,
    category: ExpenseCategory,
    frequency: String,
    billingDay: Int
  ) {
    val uid = _activeUserId.value ?: return
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val recurring = RecurringExpense(
        id = UUID.randomUUID().toString(),
        userId = uid,
        title = title.trim(),
        description = title.trim(),
        amount = amount,
        category = category.name,
        date = now,
        recurrenceInterval = frequency,
        frequency = frequency,
        billingDay = billingDay,
        startDate = now,
        nextDueDate = RecurringExpenseEngine.calculateNextDueDate(frequency, billingDay, now),
        lastProcessedDate = 0L,
        isActive = true
      )
      repository.saveRecurringExpense(recurring)
    }
  }

  fun toggleRecurringActive(recurring: RecurringExpense) {
    viewModelScope.launch {
      repository.saveRecurringExpense(recurring.copy(isActive = !recurring.isActive))
    }
  }

  fun deleteRecurringExpense(recurringId: String) {
    viewModelScope.launch {
      repository.deleteRecurringExpense(recurringId)
    }
  }

  // Budget Goals
  fun saveBudgetGoal(category: ExpenseCategory, limit: Double) {
    val uid = _activeUserId.value ?: return
    viewModelScope.launch {
      val goal = BudgetGoal(
        id = "bg_${category.name.lowercase()}",
        userId = uid,
        category = category.name,
        monthlyLimit = limit
      )
      repository.saveBudgetGoal(goal)
    }
  }

  fun updateMonthlyBudgetGoal(goal: Double) {
    if (goal <= 0.0) return
    _overrideMonthlyBudgetGoal.value = goal
    viewModelScope.launch {
      try {
        repository.updateMonthlyBudgetGoal(goal)
      } catch (e: Exception) {
        android.util.Log.e("SpendFlowVM", "Failed saving monthly budget goal", e)
      }
    }
  }

  // Subscription & Paywall Actions
  fun subscribeToTier(tier: SubscriptionTier) {
    viewModelScope.launch {
      val expiry = when (tier) {
        SubscriptionTier.PRO_MONTHLY -> System.currentTimeMillis() + (30L * 86400000L)
        SubscriptionTier.PRO_ANNUAL -> System.currentTimeMillis() + (365L * 86400000L)
        SubscriptionTier.PRO_LIFETIME -> Long.MAX_VALUE
        SubscriptionTier.FREE -> 0L
      }
      val isProUser = tier != SubscriptionTier.FREE
      val tierString = when (tier) {
        SubscriptionTier.PRO_MONTHLY -> "pro_monthly"
        SubscriptionTier.PRO_ANNUAL -> "pro_annual"
        SubscriptionTier.PRO_LIFETIME -> "pro_lifetime"
        SubscriptionTier.FREE -> "free"
      }
      repository.updateSubscriptionTier(tierString, isProUser, expiry)
    }
  }

  fun applyPromoCode(code: String): Boolean {
    val clean = code.trim().uppercase()
    return if (clean in listOf("PROMO50", "SPENDFREE", "VIP2026", "SAVE100")) {
      subscribeToTier(SubscriptionTier.PRO_ANNUAL)
      true
    } else {
      false
    }
  }

  fun signOut() {
    auth.signOut()
  }
}
