@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.ExpenseCategory

@Composable
fun AddExpenseDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, category: ExpenseCategory, note: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
  var note by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add New Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Expense Title / Merchant") },
          placeholder = { Text("e.g. Grocery Run, Coffee, Gas") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($)") },
          placeholder = { Text("0.00") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
        )

        Text("Select Category", style = MaterialTheme.typography.labelMedium)

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          ExpenseCategory.entries.forEach { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category },
              label = { Text(category.displayName, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note / Receipt details (optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmount = amountText.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter an expense title."
            return@Button
          }
          if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter a valid amount greater than 0."
            return@Button
          }
          onConfirm(title, parsedAmount, selectedCategory, note)
          onDismiss()
        },
        modifier = Modifier.testTag("save_expense_button")
      ) {
        Text("Save Expense")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun AddRecurringDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, category: ExpenseCategory, frequency: String, billingDay: Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.SUBSCRIPTIONS) }
  var frequency by remember { mutableStateOf("monthly") }
  var billingDay by remember { mutableIntStateOf(1) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Autorenew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Recurring Bill", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Subscription / Bill Name") },
          placeholder = { Text("e.g. Netflix, Spotify, Gym, Rent") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("recurring_title_input")
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($)") },
          placeholder = { Text("0.00") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("recurring_amount_input")
        )

        Text("Billing Frequency", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("monthly", "weekly", "yearly").forEach { freq ->
            FilterChip(
              selected = frequency == freq,
              onClick = { frequency = freq },
              label = { Text(freq.replaceFirstChar { it.uppercase() }) }
            )
          }
        }

        if (frequency == "monthly") {
          Text(
            text = "Billing Day of Month: Day $billingDay",
            style = MaterialTheme.typography.labelMedium
          )
          Slider(
            value = billingDay.toFloat(),
            onValueChange = { billingDay = it.toInt() },
            valueRange = 1f..31f,
            steps = 29,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Text("Category", style = MaterialTheme.typography.labelMedium)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ExpenseCategory.entries.take(5).forEach { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category },
              label = { Text(category.displayName, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Auto-Logging: SpendFlow will automatically create an expense entry on this billing day every cycle.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
          )
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmount = amountText.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter a subscription title."
            return@Button
          }
          if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter a valid amount."
            return@Button
          }
          onConfirm(title, parsedAmount, selectedCategory, frequency, billingDay)
          onDismiss()
        },
        modifier = Modifier.testTag("save_recurring_button")
      ) {
        Text("Save & Automate")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
