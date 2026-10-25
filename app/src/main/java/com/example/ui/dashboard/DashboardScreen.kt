package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SlateSurfaceCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
  userProfile: UserProfile?,
  expenses: List<Expense>,
  recurringExpenses: List<RecurringExpense>,
  isPro: Boolean,
  onAddExpenseClicked: () -> Unit,
  onNavigateToRecurring: () -> Unit,
  onNavigateToAnalytics: () -> Unit,
  onOpenPaywall: () -> Unit,
  onTriggerAutoLog: () -> Unit,
  onDeleteExpense: (String) -> Unit,
  monthlyBudgetGoal: Double = userProfile?.monthlyBudgetGoal ?: 3200.0,
  onUpdateMonthlyBudgetGoal: (Double) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val monthlyIncome = userProfile?.monthlyIncome ?: 5200.0
  val totalSpent = expenses.sumOf { it.amount }
  val netSavings = monthlyIncome - totalSpent
  val savingsRate = if (monthlyIncome > 0) ((netSavings / monthlyIncome) * 100).coerceIn(0.0, 100.0) else 0.0

  val activeRecurring = recurringExpenses.filter { it.isActive }
  val monthlyRecurringSum = activeRecurring.sumOf {
    when (it.frequency.lowercase()) {
      "weekly" -> it.amount * 4.33
      "yearly" -> it.amount / 12.0
      else -> it.amount
    }
  }

  var showAutoLogToast by remember { mutableStateOf(false) }
  var showSetBudgetDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Greeting & Pro Status Banner
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = userProfile?.displayName ?: "SpendFlow User",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        ProBadge(
          isPro = isPro,
          onClick = onOpenPaywall,
          modifier = Modifier.testTag("dashboard_pro_badge")
        )
      }
    }

    // Hero Balance Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_balance_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(
                  Color(0xFF0F2B48),
                  Color(0xFF133B5C),
                  Color(0xFF0D1B2A)
                )
              )
            )
            .padding(22.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "OCTOBER SPENDING",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
              )
              Surface(
                color = IncomeGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = String.format(Locale.US, "Savings Rate: %.0f%%", savingsRate),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = IncomeGreen,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = String.format(Locale.US, "$%.2f", totalSpent),
              style = MaterialTheme.typography.displayLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.White.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "Monthly Income",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8)
                )
                Text(
                  text = String.format(Locale.US, "$%.2f", monthlyIncome),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = IncomeGreen
                )
              }

              Column {
                Text(
                  text = "Net Cash Remaining",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8)
                )
                Text(
                  text = String.format(Locale.US, "$%.2f", netSavings),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = if (netSavings >= 0) Color.White else ExpenseCoral
                )
              }
            }
          }
        }
      }
    }

    // Quick Action Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onAddExpenseClicked,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_add_expense_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Expense", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
          onClick = onNavigateToAnalytics,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_view_charts_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("View Charts", style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    // Monthly Spending Budget Goal & Progress Bar Component
    item {
      MonthlyBudgetProgressCard(
        totalSpent = totalSpent,
        monthlyBudgetGoal = monthlyBudgetGoal,
        currency = userProfile?.currency ?: "$",
        onEditBudgetGoal = { showSetBudgetDialog = true }
      )
    }

    // Automated Recurring Subscriptions Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("recurring_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Autorenew,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Automated Recurring Bills",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${activeRecurring.size} active • ~$${String.format(Locale.US, "%.0f", monthlyRecurringSum)}/mo",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            IconButton(
              onClick = {
                onTriggerAutoLog()
                showAutoLogToast = true
              },
              modifier = Modifier.testTag("trigger_autolog_button")
            ) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync and run automated monthly logging",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          if (showAutoLogToast) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Automated monthly recurring check executed successfully!",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Preview next 2 upcoming bills
          val upcoming = activeRecurring.take(2)
          for (bill in upcoming) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = bill.title,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = "$${String.format(Locale.US, "%.2f", bill.amount)} (Day ${bill.billingDay})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          TextButton(
            onClick = onNavigateToRecurring,
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("Manage Subscriptions", style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    // Recent Expenses Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${expenses.size} items",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Transactions list
    if (expenses.isEmpty()) {
      item {
        EmptyStateCard(
          message = "No transactions logged yet",
          tip = "Tap 'Add Expense' above to log your first purchase or track spending."
        )
      }
    } else {
      items(expenses.take(8), key = { it.id }) { expense ->
        val cat = ExpenseCategory.fromString(expense.category)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val dateString = dateFormat.format(Date(expense.timestamp))

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_item_${expense.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              CategoryIconBox(category = cat, size = 40)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = expense.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  if (expense.isRecurring) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = "Auto-Logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = String.format(Locale.US, "-$%.2f", expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              IconButton(
                onClick = { onDeleteExpense(expense.id) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete expense",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog to set or edit monthly spending budget goal
  if (showSetBudgetDialog) {
    SetMonthlyBudgetDialog(
      currentGoal = monthlyBudgetGoal,
      monthlyIncome = monthlyIncome,
      currency = userProfile?.currency ?: "$",
      onDismiss = { showSetBudgetDialog = false },
      onConfirm = { newGoal ->
        onUpdateMonthlyBudgetGoal(newGoal)
      }
    )
  }
}
