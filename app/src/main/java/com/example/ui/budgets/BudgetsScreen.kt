@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.BudgetGoal
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.ui.components.BudgetProgressBar
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.SlateSurfaceCard
import java.util.Locale

@Composable
fun BudgetsScreen(
  budgetGoals: List<BudgetGoal>,
  expenses: List<Expense>,
  onSaveBudgetGoal: (ExpenseCategory, Double) -> Unit,
  modifier: Modifier = Modifier
) {
  var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
  var budgetLimitInput by remember { mutableStateOf("") }

  val totalBudgetLimit = budgetGoals.sumOf { it.monthlyLimit }
  val totalSpentAcrossBudgeted = budgetGoals.sumOf { goal ->
    expenses.filter { it.category.equals(goal.category, ignoreCase = true) }
      .sumOf { it.amount }
  }
  val remainingBudget = totalBudgetLimit - totalSpentAcrossBudgeted

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Screen Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Category Budgets",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Set monthly spending limits & guardrails",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Hero Total Budget Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Monthly Budget Pool",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              else ExpenseCoral.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = if (remainingBudget >= 0) "Under Budget" else "Over Budget!",
                style = MaterialTheme.typography.labelSmall,
                color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary else ExpenseCoral,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = String.format(Locale.US, "$%.2f Left", remainingBudget),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = if (remainingBudget >= 0) MaterialTheme.colorScheme.onSurface else ExpenseCoral
          )

          Spacer(modifier = Modifier.height(14.dp))
          Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "Budget Allocated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = String.format(Locale.US, "$%.0f", totalBudgetLimit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(text = "Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = String.format(Locale.US, "$%.2f", totalSpentAcrossBudgeted), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // List of Category Budgets
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Monthly Category Limits",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Button(
          onClick = {
            // Find a category that hasn't been added yet or default to Food
            editingCategory = ExpenseCategory.FOOD
            budgetLimitInput = "500"
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("set_budget_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Set Limit", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    if (budgetGoals.isEmpty()) {
      item {
        EmptyStateCard(
          message = "No budget limits configured",
          tip = "Tap 'Set Limit' to establish monthly caps for food, housing, or utilities."
        )
      }
    } else {
      items(budgetGoals, key = { it.id }) { goal ->
        val cat = ExpenseCategory.fromString(goal.category)
        val spent = expenses.filter { it.category.equals(goal.category, ignoreCase = true) }
          .sumOf { it.amount }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_goal_${goal.id}")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBox(category = cat, size = 38)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = String.format(Locale.US, "$%.0f monthly limit", goal.monthlyLimit),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(onClick = {
                editingCategory = cat
                budgetLimitInput = goal.monthlyLimit.toInt().toString()
              }) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit limit",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BudgetProgressBar(
              categoryName = cat.displayName,
              categoryColor = cat.color,
              spentAmount = spent,
              limitAmount = goal.monthlyLimit,
              currency = "$"
            )
          }
        }
      }
    }
  }

  // Edit / Add Budget Goal Dialog
  if (editingCategory != null) {
    var selectedCat by remember { mutableStateOf(editingCategory ?: ExpenseCategory.FOOD) }

    AlertDialog(
      onDismissRequest = { editingCategory = null },
      title = { Text("Set Monthly Budget Limit") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Category", style = MaterialTheme.typography.labelMedium)
          // Simple category selector row
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            ExpenseCategory.entries.take(6).forEach { cat ->
              FilterChip(
                selected = selectedCat == cat,
                onClick = { selectedCat = cat },
                label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }

          OutlinedTextField(
            value = budgetLimitInput,
            onValueChange = { budgetLimitInput = it },
            label = { Text("Monthly Limit ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("budget_limit_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val limit = budgetLimitInput.toDoubleOrNull() ?: 500.0
            onSaveBudgetGoal(selectedCat, limit)
            editingCategory = null
          },
          modifier = Modifier.testTag("save_budget_button")
        ) {
          Text("Save Limit")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingCategory = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
