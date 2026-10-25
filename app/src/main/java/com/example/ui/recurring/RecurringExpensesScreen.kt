package com.example.ui.recurring

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseCategory
import com.example.model.RecurringExpense
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SlateSurfaceCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecurringExpensesScreen(
  recurringExpenses: List<RecurringExpense>,
  isPro: Boolean,
  onAddRecurringClicked: () -> Unit,
  onToggleActive: (RecurringExpense) -> Unit,
  onDeleteRecurring: (String) -> Unit,
  onTriggerAutoLog: () -> Unit,
  onOpenPaywall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalMonthlyCommitment = recurringExpenses.filter { it.isActive }.sumOf {
    when (it.frequency.lowercase()) {
      "weekly" -> it.amount * 4.33
      "yearly" -> it.amount / 12.0
      else -> it.amount
    }
  }

  var syncFeedback by remember { mutableStateOf(false) }

  val freeTierLimit = 3
  val hasReachedFreeLimit = !isPro && recurringExpenses.size >= freeTierLimit

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Summary
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Recurring & Bills",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Automated monthly ledger logging",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = {
            onTriggerAutoLog()
            syncFeedback = true
          },
          modifier = Modifier.testTag("sync_recurring_button")
        ) {
          Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Sync now",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    if (syncFeedback) {
      item {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Automated check ran! Subscriptions due for this cycle were logged to your expenses ledger.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // Hero Total Recurring Monthly Card
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
              text = "Total Monthly Subscriptions",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "${recurringExpenses.count { it.isActive }} Active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = String.format(Locale.US, "$%.2f / mo", totalMonthlyCommitment),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Expenses are automatically registered in your ledger on their billing day.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Tier usage banner
    item {
      if (hasReachedFreeLimit) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.15f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Free Tier Limit Reached (${recurringExpenses.size}/$freeTierLimit)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Upgrade to Pro to track and automate unlimited recurring expenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = onOpenPaywall,
              colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Upgrade", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Subscriptions List Header & Add Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Subscriptions & Bills",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Button(
          onClick = {
            if (hasReachedFreeLimit) onOpenPaywall() else onAddRecurringClicked()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("add_recurring_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add New", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    if (recurringExpenses.isEmpty()) {
      item {
        EmptyStateCard(
          message = "No recurring subscriptions yet",
          tip = "Add Netflix, Spotify, Rent, Gym or any monthly bill to automate your expense logs.",
          icon = Icons.Default.Autorenew
        )
      }
    } else {
      items(recurringExpenses, key = { it.id }) { recurring ->
        val cat = ExpenseCategory.fromString(recurring.category)
        val nextDueFormatted = if (recurring.nextDueDate > 0) {
          SimpleDateFormat("MMM d", Locale.US).format(Date(recurring.nextDueDate))
        } else {
          "Day ${recurring.billingDay} of month"
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recurring_item_${recurring.id}")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                CategoryIconBox(category = cat, size = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = recurring.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${recurring.frequency.replaceFirstChar { it.uppercase() }} • Next due: $nextDueFormatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = String.format(Locale.US, "$%.2f", recurring.amount),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                  checked = recurring.isActive,
                  onCheckedChange = { onToggleActive(recurring) },
                  modifier = Modifier.testTag("toggle_recurring_${recurring.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (recurring.isActive) "Auto-logging Active" else "Paused",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (recurring.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              IconButton(
                onClick = { onDeleteRecurring(recurring.id) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete subscription",
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
}
