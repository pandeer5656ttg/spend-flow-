package com.example.data.local

typealias RecurringExpenseEntity = RecurringExpense

fun RecurringExpense.toEntity(): RecurringExpense = this
fun RecurringExpense.toDomain(): RecurringExpense = this
