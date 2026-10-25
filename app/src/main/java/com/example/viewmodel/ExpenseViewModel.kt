package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Expense
import com.example.data.local.ExpenseDao
import com.example.data.local.RecurringExpense
import com.example.data.local.RecurringExpenseDao
import com.example.data.local.SpendFlowDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ExpenseViewModel manages local expense state and provides insertion and retrieval
 * operations backed by Room DAOs.
 */
class ExpenseViewModel(
  private val expenseDao: ExpenseDao,
  private val recurringExpenseDao: RecurringExpenseDao? = null
) : ViewModel() {

  // Convenience constructor accepting SpendFlowDatabase
  constructor(database: SpendFlowDatabase) : this(
    expenseDao = database.expenseDao(),
    recurringExpenseDao = database.recurringExpenseDao()
  )

  // Convenience constructor accepting Context for Compose ViewModel factories
  constructor(context: Context) : this(
    SpendFlowDatabase.getDatabase(context.applicationContext)
  )

  private val _selectedUserId = MutableStateFlow<String>("")
  val selectedUserId: StateFlow<String> = _selectedUserId.asStateFlow()

  private val _searchQuery = MutableStateFlow<String>("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow<String?>(null)
  val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

  private val _isLoading = MutableStateFlow<Boolean>(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Primary expenses stream: watches user-scoped expenses if userId is set, otherwise all expenses
  @OptIn(ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<Expense>> = _selectedUserId
    .flatMapLatest { uid ->
      if (uid.isNotBlank()) {
        expenseDao.getExpensesByUserId(uid)
      } else {
        expenseDao.getAllExpenses()
      }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Primary recurring expenses stream
  @OptIn(ExperimentalCoroutinesApi::class)
  val recurringExpenses: StateFlow<List<RecurringExpense>> = _selectedUserId
    .flatMapLatest { uid ->
      val dao = recurringExpenseDao
      if (dao != null) {
        if (uid.isNotBlank()) dao.getRecurringExpensesByUserId(uid) else dao.getAllRecurringExpenses()
      } else {
        flowOf(emptyList())
      }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // Total amount spent across the current expenses stream
  val totalExpenseAmount: StateFlow<Double> = expenses
    .map { list -> list.sumOf { it.amount } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0.0
    )

  // Filtered expenses based on search query and category filter
  val filteredExpenses: StateFlow<List<Expense>> = combine(
    expenses,
    _searchQuery,
    _selectedCategory
  ) { list, query, category ->
    list.filter { item ->
      val matchesQuery = query.isBlank() ||
          item.description.contains(query, ignoreCase = true) ||
          item.title.contains(query, ignoreCase = true) ||
          item.note.contains(query, ignoreCase = true)

      val matchesCategory = category.isNullOrBlank() ||
          item.category.equals(category, ignoreCase = true)

      matchesQuery && matchesCategory
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // --- RETRIEVAL & FILTERING FUNCTIONS ---

  fun setUserId(userId: String) {
    _selectedUserId.value = userId
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedCategory(category: String?) {
    _selectedCategory.value = category
  }

  fun getExpensesForUser(userId: String): Flow<List<Expense>> {
    return expenseDao.getExpensesByUserId(userId)
  }

  fun getRecurringExpensesForUser(userId: String): Flow<List<RecurringExpense>> {
    return recurringExpenseDao?.getRecurringExpensesByUserId(userId) ?: flowOf(emptyList())
  }

  fun getExpenseById(id: String, onResult: (Expense?) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = expenseDao.getExpenseById(id)
      onResult(result)
    }
  }

  fun getRecurringExpenseById(id: String, onResult: (RecurringExpense?) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = recurringExpenseDao?.getRecurringExpenseById(id)
      onResult(result)
    }
  }

  // --- INSERTION & MUTATION FUNCTIONS ---

  fun insertExpense(expense: Expense) {
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        expenseDao.insertExpense(expense)
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun insertExpense(
    amount: Double,
    category: String,
    description: String,
    date: Long = System.currentTimeMillis(),
    recurrenceInterval: String = "",
    note: String = "",
    userId: String = _selectedUserId.value
  ) {
    val newExpense = Expense(
      id = UUID.randomUUID().toString(),
      userId = userId,
      amount = amount,
      category = category,
      date = date,
      description = description,
      recurrenceInterval = recurrenceInterval,
      title = description,
      timestamp = date,
      note = note
    )
    insertExpense(newExpense)
  }

  fun insertExpenses(expenses: List<Expense>) {
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        expenseDao.insertExpenses(expenses)
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateExpense(expense: Expense) {
    viewModelScope.launch(Dispatchers.IO) {
      expenseDao.updateExpense(expense)
    }
  }

  fun deleteExpense(expense: Expense) {
    deleteExpenseById(expense.id)
  }

  fun deleteExpenseById(id: String) {
    viewModelScope.launch(Dispatchers.IO) {
      expenseDao.deleteExpenseById(id)
    }
  }

  fun deleteExpensesForUser(userId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      expenseDao.deleteExpensesForUser(userId)
    }
  }

  // --- RECURRING EXPENSE MUTATIONS ---

  fun insertRecurringExpense(recurringExpense: RecurringExpense) {
    val dao = recurringExpenseDao ?: return
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        dao.insertRecurringExpense(recurringExpense)
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun insertRecurringExpense(
    amount: Double,
    category: String,
    description: String,
    recurrenceInterval: String = "monthly",
    billingDay: Int = 1,
    date: Long = System.currentTimeMillis(),
    userId: String = _selectedUserId.value
  ) {
    val newRecurring = RecurringExpense(
      id = UUID.randomUUID().toString(),
      userId = userId,
      amount = amount,
      category = category,
      date = date,
      description = description,
      recurrenceInterval = recurrenceInterval,
      title = description,
      frequency = recurrenceInterval,
      billingDay = billingDay,
      startDate = date,
      nextDueDate = date,
      isActive = true
    )
    insertRecurringExpense(newRecurring)
  }

  fun insertRecurringExpenses(recurringList: List<RecurringExpense>) {
    val dao = recurringExpenseDao ?: return
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        dao.insertRecurringExpenses(recurringList)
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateRecurringExpense(recurringExpense: RecurringExpense) {
    val dao = recurringExpenseDao ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.updateRecurringExpense(recurringExpense)
    }
  }

  fun toggleRecurringActive(recurringExpense: RecurringExpense) {
    val dao = recurringExpenseDao ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.updateActiveStatus(recurringExpense.id, !recurringExpense.isActive)
    }
  }

  fun deleteRecurringExpense(recurringExpense: RecurringExpense) {
    deleteRecurringExpenseById(recurringExpense.id)
  }

  fun deleteRecurringExpenseById(id: String) {
    val dao = recurringExpenseDao ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.deleteRecurringExpenseById(id)
    }
  }
}
