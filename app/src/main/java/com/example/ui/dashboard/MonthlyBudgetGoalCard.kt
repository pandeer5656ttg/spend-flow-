package com.example.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/**
 * MonthlyBudgetProgressCard is a prominent visual component on the Main Dashboard
 * indicating the user's spending against their monthly budget goal, with a multi-threshold
 * progress bar and calculated daily remaining spending allowance.
 */
@Composable
fun MonthlyBudgetProgressCard(
  totalSpent: Double,
  monthlyBudgetGoal: Double,
  currency: String = "$",
  onEditBudgetGoal: () -> Unit,
  modifier: Modifier = Modifier
) {
  val goal = if (monthlyBudgetGoal > 0) monthlyBudgetGoal else 3200.0
  val remaining = goal - totalSpent
  val spentRatio = (totalSpent / goal).toFloat()
  val progress = spentRatio.coerceIn(0f, 1f)
  val isOverBudget = totalSpent > goal
  val isNearLimit = spentRatio in 0.75f..1.0f

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 800),
    label = "budget_progress"
  )

  // Dynamic calendar calculation for days left in month
  val calendar = Calendar.getInstance()
  val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
  val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val daysRemaining = max(1, totalDaysInMonth - currentDay + 1)
  val monthName = SimpleDateFormat("MMMM", Locale.US).format(calendar.time)

  val dailyAllowance = if (remaining > 0) remaining / daysRemaining else 0.0

  val statusColor = when {
    isOverBudget -> ExpenseCoral
    isNearLimit -> Color(0xFFF59E0B) // Amber
    else -> IncomeGreen
  }

  val statusGradient = when {
    isOverBudget -> listOf(ExpenseCoral, Color(0xFFDC2626))
    isNearLimit -> listOf(Color(0xFFFBBF24), Color(0xFFD97706))
    else -> listOf(IncomeGreen, Color(0xFF059669))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("dashboard_monthly_budget_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header: Icon, Title, and Edit button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.TrackChanges,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Monthly Spending Budget",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$monthName Budget Target",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Set / Edit Budget Goal Button
        FilledTonalButton(
          onClick = onEditBudgetGoal,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("edit_monthly_budget_button"),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Goal",
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Set Goal",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Financial Metrics Row: Spent vs Goal & Remaining Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "Total Spent This Month",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = String.format(Locale.US, "%s%.2f", currency, totalSpent),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = String.format(Locale.US, " / %s%.0f", currency, goal),
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
          }
        }

        // Status pill indicator
        Surface(
          color = statusColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isOverBudget) {
                String.format(Locale.US, "+%s%.0f Over", currency, -remaining)
              } else {
                String.format(Locale.US, "%s%.0f Left", currency, remaining)
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = statusColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Visual Progress Bar Component
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(12.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .testTag("dashboard_budget_progress_bar")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(fraction = animatedProgress)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.horizontalGradient(statusGradient))
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Progress percentages and metrics footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = String.format(Locale.US, "%.1f%% of budget spent", spentRatio * 100),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = statusColor
        )

        Text(
          text = if (remaining > 0) {
            String.format(Locale.US, "%.1f%% remaining", (1f - spentRatio).coerceAtLeast(0f) * 100)
          } else {
            "Budget exceeded"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Divider(color = SlateBorder.copy(alpha = 0.6f))

      Spacer(modifier = Modifier.height(10.dp))

      // Daily Runway & Smart Pace Breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Savings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "$daysRemaining days left in $monthName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = if (remaining > 0) {
            String.format(Locale.US, "~%s%.2f/day available", currency, dailyAllowance)
          } else {
            "Daily limit exhausted"
          },
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = if (remaining > 0) MaterialTheme.colorScheme.onSurface else ExpenseCoral
        )
      }
    }
  }
}

/**
 * SetMonthlyBudgetDialog allows users to adjust their monthly spending budget goal
 * with quick preset options and live daily allowance preview.
 */
@Composable
fun SetMonthlyBudgetDialog(
  currentGoal: Double,
  monthlyIncome: Double,
  currency: String = "$",
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var inputAmount by remember {
    mutableStateOf(if (currentGoal > 0) currentGoal.toInt().toString() else "3200")
  }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val calendar = Calendar.getInstance()
  val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
  val daysRemaining = max(1, totalDaysInMonth - currentDay + 1)

  val parsedGoal = inputAmount.toDoubleOrNull() ?: 0.0
  val estimatedDaily = if (parsedGoal > 0) parsedGoal / totalDaysInMonth else 0.0

  val presetAmounts = listOf(
    1500.0,
    2500.0,
    3000.0,
    3500.0,
    4000.0,
    5000.0
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("set_monthly_budget_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.TrackChanges,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Monthly Budget Goal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Set your total monthly spending target",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Establishing a budget target updates your dashboard progress bar, spending pace alerts, and daily allowance.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Amount Input Field
        OutlinedTextField(
          value = inputAmount,
          onValueChange = { newValue ->
            // Allow only digits and decimal point
            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
              inputAmount = newValue
              errorMessage = null
            }
          },
          label = { Text("Monthly Budget Amount") },
          prefix = {
            Text(
              currency,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary
            )
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          isError = errorMessage != null,
          supportingText = {
            if (errorMessage != null) {
              Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (parsedGoal > 0) {
              Text(
                text = String.format(Locale.US, "≈ %s%.2f per day across %d days", currency, estimatedDaily, totalDaysInMonth),
                color = MaterialTheme.colorScheme.primary
              )
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_budget_goal_input")
        )

        // Quick Preset Chips
        Column {
          Text(
            text = "Quick Presets",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
          ) {
            items(presetAmounts) { preset ->
              val isSelected = parsedGoal.toInt() == preset.toInt()
              FilterChip(
                selected = isSelected,
                onClick = { inputAmount = preset.toInt().toString() },
                label = {
                  Text(
                    String.format(Locale.US, "%s%.0f", currency, preset),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                modifier = Modifier.testTag("budget_preset_${preset.toInt()}")
              )
            }

            // Income-proportional chip if monthlyIncome is set
            if (monthlyIncome > 1000) {
              val seventyPercent = (monthlyIncome * 0.70).toInt()
              item {
                FilterChip(
                  selected = parsedGoal.toInt() == seventyPercent,
                  onClick = { inputAmount = seventyPercent.toString() },
                  label = { Text("70% of Income ($currency$seventyPercent)") },
                  modifier = Modifier.testTag("budget_preset_income_70")
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val goal = inputAmount.toDoubleOrNull()
          if (goal == null || goal <= 0) {
            errorMessage = "Please enter a valid amount greater than 0"
          } else {
            onConfirm(goal)
            onDismiss()
          }
        },
        modifier = Modifier.testTag("save_monthly_budget_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Save Goal")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_monthly_budget_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
