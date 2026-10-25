package com.example.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.EmptyStateCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseListView(
  expenses: List<Expense>,
  onAddExpenseClicked: () -> Unit,
  onDeleteExpense: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<String?>(null) }

  val filteredExpenses = remember(expenses, searchQuery, selectedCategory) {
    expenses.filter { item ->
      val matchesSearch = searchQuery.isBlank() ||
          item.displayTitle.contains(searchQuery, ignoreCase = true) ||
          item.note.contains(searchQuery, ignoreCase = true)

      val matchesCategory = selectedCategory == null ||
          item.category.equals(selectedCategory, ignoreCase = true)

      matchesSearch && matchesCategory
    }
  }

  val totalFilteredAmount = remember(filteredExpenses) {
    filteredExpenses.sumOf { it.amount }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("expense_search_input"),
      placeholder = { Text("Search transactions, merchants, notes...") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear search")
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Category Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { selectedCategory = null },
          label = { Text("All") },
          shape = RoundedCornerShape(12.dp)
        )
      }
      items(ExpenseCategory.entries) { cat ->
        FilterChip(
          selected = selectedCategory.equals(cat.name, ignoreCase = true),
          onClick = {
            selectedCategory = if (selectedCategory == cat.name) null else cat.name
          },
          label = { Text(cat.displayName) },
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Summary Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${filteredExpenses.size} Transactions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = String.format(Locale.US, "Total: $%.2f", totalFilteredAmount),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Expense List
    if (filteredExpenses.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        EmptyStateCard(
          message = if (searchQuery.isNotBlank() || selectedCategory != null) "No matching expenses found" else "No expenses logged yet",
          tip = if (searchQuery.isNotBlank() || selectedCategory != null) "Try adjusting your search query or filters" else "Tap the '+' button below to add your first expense.",
          icon = Icons.Default.ReceiptLong
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredExpenses, key = { it.id }) { expense ->
          val cat = ExpenseCategory.fromString(expense.category)
          val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
          val dateString = dateFormat.format(Date(expense.displayDate))

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("expense_card_${expense.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
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
                CategoryIconBox(category = cat, size = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = expense.displayTitle,
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
                  if (expense.note.isNotBlank() && expense.note != expense.displayTitle) {
                    Text(
                      text = expense.note,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1
                    )
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
                  modifier = Modifier.size(36.dp)
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
  }
}
