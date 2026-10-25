package com.example.ui.analytics

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategorySpend
import com.example.model.DailySpend
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.DailySpendBarChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SlateSurfaceCard
import java.util.Locale

@Composable
fun AnalyticsScreen(
  expenses: List<Expense>,
  categorySpends: List<CategorySpend>,
  dailySpends: List<DailySpend>,
  totalSpent: Double,
  isPro: Boolean,
  onOpenPaywall: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("This Month") }
  var showExportDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Financial Analytics",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Data visualization & spend breakdown",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = {
            if (isPro) showExportDialog = true else onOpenPaywall()
          },
          modifier = Modifier.testTag("export_analytics_button")
        ) {
          Icon(
            imageVector = if (isPro) Icons.Default.FileDownload else Icons.Default.Lock,
            contentDescription = "Export Report",
            tint = if (isPro) MaterialTheme.colorScheme.primary else AccentGold
          )
        }
      }
    }

    // Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("This Month", "Last 30 Days", "Year to Date").forEach { filter ->
          FilterChip(
            selected = selectedFilter == filter,
            onClick = { selectedFilter = filter },
            label = { Text(filter) },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        }
      }
    }

    // Donut Chart Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("category_donut_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Category Distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(14.dp))

          if (categorySpends.isEmpty()) {
            EmptyStateCard(
              message = "No expense data yet",
              tip = "Log some transactions to see your spending donut chart."
            )
          } else {
            CategoryDonutChart(
              categorySpends = categorySpends,
              totalAmount = totalSpent
            )
          }
        }
      }
    }

    // Daily Spending Trend Bar Chart
    item {
      DailySpendBarChart(
        dailySpends = dailySpends,
        currency = "$"
      )
    }

    // Pro Insights or Upgrade Pitch
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isPro) MaterialTheme.colorScheme.surfaceVariant
          else AccentGold.copy(alpha = 0.12f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { if (!isPro) onOpenPaywall() }
          .testTag("pro_insights_card")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(if (isPro) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else AccentGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPro) Icons.Default.TrendingUp else Icons.Default.Lock,
              contentDescription = null,
              tint = if (isPro) MaterialTheme.colorScheme.primary else AccentGold,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isPro) "Smart Spending Velocity" else "Unlock Pro Smart Forecasts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (isPro)
                "Your daily average is $${String.format(Locale.US, "%.1f", totalSpent / 25)}. At this rate, your projected monthly savings will exceed $2,600."
              else
                "Predict end-of-month cashflow, overspend risks, and export full reports with SpendFlow Pro.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (!isPro) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = AccentGold
            )
          }
        }
      }
    }

    // Detailed Category Breakdown List
    item {
      Text(
        text = "Category Breakdown",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    items(categorySpends, key = { it.category.name }) { spend ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
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
            CategoryIconBox(category = spend.category, size = 38)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = spend.category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${spend.count} transactions • ${String.format(Locale.US, "%.1f%% of spend", spend.percentage)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = String.format(Locale.US, "$%.2f", spend.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = spend.category.color
          )
        }
      }
    }
  }

  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Financial Summary") },
      text = {
        Text("Your detailed CSV and financial breakdown for October 2026 has been prepared. Total expenditures: $${String.format(Locale.US, "%.2f", totalSpent)} across ${categorySpends.size} categories.")
      },
      confirmButton = {
        Button(onClick = { showExportDialog = false }) {
          Text("Download CSV")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}
