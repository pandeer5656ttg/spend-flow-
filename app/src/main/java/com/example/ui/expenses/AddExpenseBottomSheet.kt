@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.expenses

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseCategory
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
  onDismiss: () -> Unit,
  onSaveExpense: (title: String, amount: Double, category: ExpenseCategory, date: Long, note: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  var amountInput by remember { mutableStateOf("") }
  var titleInput by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
  var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var noteInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showDatePickerDialog by remember { mutableStateOf(false) }

  val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.US) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = { BottomSheetDefaults.DragHandle() },
    modifier = Modifier.testTag("add_expense_modal_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
        .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Add New Expense",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Record purchase details and category",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close sheet")
        }
      }

      // 1. Amount Field with Quick Increment Chips
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Amount",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = amountInput,
          onValueChange = {
            if (it.count { ch -> ch == '.' } <= 1 && it.all { ch -> ch.isDigit() || ch == '.' }) {
              amountInput = it
              errorMessage = null
            }
          },
          prefix = {
            Text(
              text = "$",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          },
          placeholder = { Text("0.00", style = MaterialTheme.typography.titleLarge) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_sheet_amount_input"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick amount add chips
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          listOf(10.0, 25.0, 50.0, 100.0).forEach { quickAmount ->
            SuggestionChip(
              onClick = {
                val current = amountInput.toDoubleOrNull() ?: 0.0
                amountInput = String.format(Locale.US, "%.2f", current + quickAmount)
              },
              label = {
                Text(
                  text = "+$${quickAmount.toInt()}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold
                )
              },
              shape = RoundedCornerShape(8.dp)
            )
          }
        }
      }

      // 2. Title / Merchant Field
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Description / Merchant",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = titleInput,
          onValueChange = {
            titleInput = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Grocery Run, Coffee, Dinner, Gas") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_sheet_title_input"),
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        )
      }

      // 3. Category Selector
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_sheet_category_row")
        ) {
          items(ExpenseCategory.entries) { category ->
            val isSelected = selectedCategory == category
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (isSelected) category.color.copy(alpha = 0.25f)
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                  width = if (isSelected) 1.5.dp else 0.dp,
                  color = if (isSelected) category.color else Color.Transparent,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedCategory = category }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(
                imageVector = getCategoryIcon(category.name),
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 4. Date Field & Selector
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Date",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, SlateBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { showDatePickerDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("expense_sheet_date_picker_button"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = "Select Date",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = dateFormat.format(Date(selectedDateMillis)),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Icon(
            imageVector = Icons.Default.EditCalendar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // 5. Optional Note Field
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Notes (Optional)",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          placeholder = { Text("Receipt reference, split details, etc.") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )
      }

      // Error Message if invalid
      if (errorMessage != null) {
        Surface(
          color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      // 6. Save Expense Action Button
      Button(
        onClick = {
          val parsedAmount = amountInput.toDoubleOrNull()
          if (titleInput.isBlank()) {
            errorMessage = "Please enter an expense description or merchant."
            return@Button
          }
          if (parsedAmount == null || parsedAmount <= 0.0) {
            errorMessage = "Please enter an amount greater than $0."
            return@Button
          }
          onSaveExpense(
            titleInput.trim(),
            parsedAmount,
            selectedCategory,
            selectedDateMillis,
            noteInput.trim()
          )
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("expense_sheet_save_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save Expense",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  // Material 3 DatePickerDialog
  if (showDatePickerDialog) {
    DatePickerDialog(
      onDismissRequest = { showDatePickerDialog = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              selectedDateMillis = millis
            }
            showDatePickerDialog = false
          }
        ) {
          Text("Select")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePickerDialog = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
