package com.example.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SubscriptionTier
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.budgets.BudgetsScreen
import com.example.ui.components.ProBadge
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dialogs.AddRecurringDialog
import com.example.ui.expenses.AddExpenseBottomSheet
import com.example.ui.expenses.ExpenseListView
import com.example.ui.recurring.RecurringExpensesScreen
import com.example.ui.subscription.SubscriptionPaywallScreen
import com.example.viewmodel.SpendFlowViewModel

/**
 * Navigation destinations for the main screen layout.
 */
enum class MainScreenTab(
  val title: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val testTag: String
) {
  DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_tab_dashboard"),
  EXPENSES("Expenses", Icons.Default.ReceiptLong, "nav_tab_expenses"),
  RECURRING("Recurring", Icons.Default.Autorenew, "nav_tab_recurring"),
  ANALYTICS("Analytics", Icons.Default.PieChart, "nav_tab_analytics"),
  BUDGETS("Budgets", Icons.Default.Savings, "nav_tab_budgets")
}

/**
 * MainScreen Composable provides the primary container layout with a top app bar,
 * a bottom navigation bar, a floating action button, and hosted views for dashboard,
 * expense list, recurring tracker, analytics, and budgets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  viewModel: SpendFlowViewModel,
  modifier: Modifier = Modifier
) {
  var currentTab by remember { mutableStateOf(MainScreenTab.DASHBOARD) }
  var showAddExpenseBottomSheet by remember { mutableStateOf(false) }
  var showAddRecurringDialog by remember { mutableStateOf(false) }
  var showPaywall by remember { mutableStateOf(false) }
  var showProfileMenu by remember { mutableStateOf(false) }

  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val expenses by viewModel.expenses.collectAsStateWithLifecycle()
  val recurringExpenses by viewModel.recurringExpenses.collectAsStateWithLifecycle()
  val budgetGoals by viewModel.budgetGoals.collectAsStateWithLifecycle()
  val categorySpends by viewModel.categorySpends.collectAsStateWithLifecycle()
  val dailySpends by viewModel.dailySpends.collectAsStateWithLifecycle()
  val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
  val isPro by viewModel.isPro.collectAsStateWithLifecycle()
  val monthlyBudgetGoal by viewModel.monthlyBudgetGoal.collectAsStateWithLifecycle()

  // Handle system back navigation to return to Dashboard or dismiss overlay sheets
  BackHandler(enabled = showPaywall || showAddExpenseBottomSheet || currentTab != MainScreenTab.DASHBOARD) {
    if (showPaywall) {
      showPaywall = false
    } else if (showAddExpenseBottomSheet) {
      showAddExpenseBottomSheet = false
    } else {
      currentTab = MainScreenTab.DASHBOARD
    }
  }

  if (showPaywall) {
    SubscriptionPaywallScreen(
      userProfile = userProfile,
      onSubscribe = { tier ->
        viewModel.subscribeToTier(tier)
      },
      onApplyPromo = { code ->
        viewModel.applyPromoCode(code)
      },
      onDismiss = { showPaywall = false }
    )
  } else {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.testTag("main_top_app_bar_title")
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(19.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "SpendFlow",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = currentTab.title,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          },
          actions = {
            // Pro Membership Pill Badge
            ProBadge(
              isPro = isPro,
              onClick = { showPaywall = true },
              modifier = Modifier
                .padding(end = 4.dp)
                .testTag("main_pro_badge")
            )

            // User Profile & Settings Menu
            Box {
              IconButton(
                onClick = { showProfileMenu = true },
                modifier = Modifier.testTag("main_profile_menu_button")
              ) {
                Icon(
                  imageVector = Icons.Default.AccountCircle,
                  contentDescription = "User profile and options",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }
              DropdownMenu(
                expanded = showProfileMenu,
                onDismissRequest = { showProfileMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text(userProfile?.email ?: "user@spendflow.app") },
                  onClick = { showProfileMenu = false },
                  enabled = false
                )
                DropdownMenuItem(
                  text = { Text(if (isPro) "Plan: Pro Active" else "Upgrade to Pro") },
                  onClick = {
                    showProfileMenu = false
                    showPaywall = true
                  },
                  leadingIcon = {
                    Icon(
                      Icons.Default.WorkspacePremium,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.tertiary
                    )
                  }
                )
                Divider()
                DropdownMenuItem(
                  text = { Text("Sign Out") },
                  onClick = {
                    showProfileMenu = false
                    viewModel.signOut()
                  },
                  leadingIcon = {
                    Icon(Icons.Default.Logout, contentDescription = null)
                  }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          )
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("main_bottom_navigation_bar")
        ) {
          MainScreenTab.entries.forEach { tab ->
            NavigationBarItem(
              selected = currentTab == tab,
              onClick = { currentTab = tab },
              icon = {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.title
                )
              },
              label = {
                Text(
                  text = tab.title,
                  style = MaterialTheme.typography.labelSmall
                )
              },
              modifier = Modifier.testTag(tab.testTag),
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              )
            )
          }
        }
      },
      floatingActionButton = {
        when (currentTab) {
          MainScreenTab.DASHBOARD, MainScreenTab.EXPENSES, MainScreenTab.ANALYTICS -> {
            FloatingActionButton(
              onClick = { showAddExpenseBottomSheet = true },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.testTag("main_fab_add_expense")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
          }
          MainScreenTab.RECURRING -> {
            FloatingActionButton(
              onClick = {
                if (!isPro && recurringExpenses.size >= 3) {
                  showPaywall = true
                } else {
                  showAddRecurringDialog = true
                }
              },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.testTag("main_fab_add_recurring")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Add Recurring Bill")
            }
          }
          MainScreenTab.BUDGETS -> {
            // No standalone FAB needed on Budgets tab as it has its own in-card header button
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(MaterialTheme.colorScheme.background)
      ) {
        when (currentTab) {
          MainScreenTab.DASHBOARD -> {
            DashboardScreen(
              userProfile = userProfile,
              expenses = expenses,
              recurringExpenses = recurringExpenses,
              isPro = isPro,
              onAddExpenseClicked = { showAddExpenseBottomSheet = true },
              onNavigateToRecurring = { currentTab = MainScreenTab.RECURRING },
              onNavigateToAnalytics = { currentTab = MainScreenTab.ANALYTICS },
              onOpenPaywall = { showPaywall = true },
              onTriggerAutoLog = { viewModel.triggerAutoRecurringCheck() },
              onDeleteExpense = { id -> viewModel.deleteExpense(id) },
              monthlyBudgetGoal = monthlyBudgetGoal,
              onUpdateMonthlyBudgetGoal = { goal -> viewModel.updateMonthlyBudgetGoal(goal) }
            )
          }
          MainScreenTab.EXPENSES -> {
            ExpenseListView(
              expenses = expenses,
              onAddExpenseClicked = { showAddExpenseBottomSheet = true },
              onDeleteExpense = { id -> viewModel.deleteExpense(id) }
            )
          }
          MainScreenTab.RECURRING -> {
            RecurringExpensesScreen(
              recurringExpenses = recurringExpenses,
              isPro = isPro,
              onAddRecurringClicked = { showAddRecurringDialog = true },
              onToggleActive = { rec -> viewModel.toggleRecurringActive(rec) },
              onDeleteRecurring = { id -> viewModel.deleteRecurringExpense(id) },
              onTriggerAutoLog = { viewModel.triggerAutoRecurringCheck() },
              onOpenPaywall = { showPaywall = true }
            )
          }
          MainScreenTab.ANALYTICS -> {
            AnalyticsScreen(
              expenses = expenses,
              categorySpends = categorySpends,
              dailySpends = dailySpends,
              totalSpent = totalSpent,
              isPro = isPro,
              onOpenPaywall = { showPaywall = true }
            )
          }
          MainScreenTab.BUDGETS -> {
            BudgetsScreen(
              budgetGoals = budgetGoals,
              expenses = expenses,
              onSaveBudgetGoal = { cat, limit ->
                viewModel.saveBudgetGoal(cat, limit)
              }
            )
          }
        }
      }
    }
  }

  // ModalBottomSheet for logging an expense
  if (showAddExpenseBottomSheet) {
    AddExpenseBottomSheet(
      onDismiss = { showAddExpenseBottomSheet = false },
      onSaveExpense = { title, amount, category, date, note ->
        viewModel.addExpense(
          title = title,
          amount = amount,
          category = category,
          note = note,
          date = date
        )
      }
    )
  }

  // Dialog for adding a recurring subscription/bill
  if (showAddRecurringDialog) {
    AddRecurringDialog(
      onDismiss = { showAddRecurringDialog = false },
      onConfirm = { title, amount, category, freq, day ->
        viewModel.addRecurringExpense(title, amount, category, freq, day)
      }
    )
  }
}
