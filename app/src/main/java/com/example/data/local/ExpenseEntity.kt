package com.example.data.local

typealias ExpenseEntity = Expense

fun Expense.toEntity(): Expense = this
fun Expense.toDomain(): Expense = this
