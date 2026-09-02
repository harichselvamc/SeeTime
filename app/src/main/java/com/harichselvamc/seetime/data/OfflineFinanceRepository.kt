package com.harichselvamc.seetime.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class OfflineExpense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val currencyCode: String = "USD",
    val category: String = "Food & Dining",
    val timestampMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class CategoryBudget(
    val category: String,
    val monthlyLimit: Double,
    val currencyCode: String = "USD"
)

data class BillReminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val currencyCode: String = "USD",
    val dueEpochMillis: Long,
    val isRecurringMonthly: Boolean = true,
    val isPaid: Boolean = false,
    val category: String = "Utilities"
)

data class BudgetStatus(
    val category: String,
    val spent: Double,
    val limit: Double,
    val percentUsed: Float,
    val isOverBudget: Boolean
)

class OfflineFinanceRepository(context: Context? = null) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("seetime_offline_finance", Context.MODE_PRIVATE)

    private val _expenses = MutableStateFlow<List<OfflineExpense>>(emptyList())
    val expenses: StateFlow<List<OfflineExpense>> = _expenses.asStateFlow()

    private val _budgets = MutableStateFlow<List<CategoryBudget>>(emptyList())
    val budgets: StateFlow<List<CategoryBudget>> = _budgets.asStateFlow()

    private val _bills = MutableStateFlow<List<BillReminder>>(emptyList())
    val bills: StateFlow<List<BillReminder>> = _bills.asStateFlow()

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Food & Dining",
            "Transport",
            "Lodging",
            "Utilities",
            "Work & Tech",
            "Entertainment",
            "Shopping",
            "General"
        )

        @Volatile
        private var INSTANCE: OfflineFinanceRepository? = null

        fun getInstance(context: Context): OfflineFinanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineFinanceRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun createSampleExpenses(): List<OfflineExpense> {
            val now = Instant.now()
            return listOf(
                OfflineExpense(
                    id = "exp-1",
                    title = "Tokyo Metro 3-Day Pass",
                    amount = 15.00,
                    currencyCode = "USD",
                    category = "Transport",
                    timestampMillis = now.minus(2, ChronoUnit.HOURS).toEpochMilli(),
                    notes = "Subway pass for Shibuya & Shinjuku travel"
                ),
                OfflineExpense(
                    id = "exp-2",
                    title = "Dinner at Ichiran Ramen",
                    amount = 18.50,
                    currencyCode = "USD",
                    category = "Food & Dining",
                    timestampMillis = now.minus(6, ChronoUnit.HOURS).toEpochMilli(),
                    notes = "Tonkotsu ramen + matcha tea"
                ),
                OfflineExpense(
                    id = "exp-3",
                    title = "Pocket Wi-Fi Terminal Rental",
                    amount = 45.00,
                    currencyCode = "USD",
                    category = "Work & Tech",
                    timestampMillis = now.minus(1, ChronoUnit.DAYS).toEpochMilli(),
                    notes = "7-day unlimited 5G hotspot"
                ),
                OfflineExpense(
                    id = "exp-4",
                    title = "Hotel Shinjuku Stay (Night 1)",
                    amount = 120.00,
                    currencyCode = "USD",
                    category = "Lodging",
                    timestampMillis = now.minus(2, ChronoUnit.DAYS).toEpochMilli(),
                    notes = "Room with Tokyo Tower view"
                )
            )
        }

        fun createSampleBudgets(): List<CategoryBudget> {
            return listOf(
                CategoryBudget(category = "Food & Dining", monthlyLimit = 400.00),
                CategoryBudget(category = "Transport", monthlyLimit = 150.00),
                CategoryBudget(category = "Lodging", monthlyLimit = 800.00),
                CategoryBudget(category = "Work & Tech", monthlyLimit = 200.00),
                CategoryBudget(category = "Entertainment", monthlyLimit = 150.00)
            )
        }

        fun createSampleBills(): List<BillReminder> {
            val now = Instant.now()
            return listOf(
                BillReminder(
                    id = "bill-1",
                    title = "International Mobile Roaming",
                    amount = 35.00,
                    currencyCode = "USD",
                    dueEpochMillis = now.plus(3, ChronoUnit.DAYS).toEpochMilli(),
                    isRecurringMonthly = true,
                    isPaid = false,
                    category = "Utilities"
                ),
                BillReminder(
                    id = "bill-2",
                    title = "Cloud Server Subscription",
                    amount = 20.00,
                    currencyCode = "USD",
                    dueEpochMillis = now.plus(8, ChronoUnit.DAYS).toEpochMilli(),
                    isRecurringMonthly = true,
                    isPaid = false,
                    category = "Work & Tech"
                ),
                BillReminder(
                    id = "bill-3",
                    title = "Travel Health Insurance",
                    amount = 65.00,
                    currencyCode = "USD",
                    dueEpochMillis = now.minus(1, ChronoUnit.DAYS).toEpochMilli(),
                    isRecurringMonthly = false,
                    isPaid = true,
                    category = "General"
                )
            )
        }
    }

    init {
        loadFromStorage()
    }

    // Expense Management
    fun addExpense(expense: OfflineExpense) {
        _expenses.value = listOf(expense) + _expenses.value.filterNot { it.id == expense.id }
        saveToStorage()
    }

    fun deleteExpense(id: String) {
        _expenses.value = _expenses.value.filterNot { it.id == id }
        saveToStorage()
    }

    // Budget Management
    fun setBudget(budget: CategoryBudget) {
        _budgets.value = listOf(budget) + _budgets.value.filterNot { it.category == budget.category }
        saveToStorage()
    }

    fun deleteBudget(category: String) {
        _budgets.value = _budgets.value.filterNot { it.category == category }
        saveToStorage()
    }

    // Bill Reminder Management
    fun addBill(bill: BillReminder) {
        _bills.value = listOf(bill) + _bills.value.filterNot { it.id == bill.id }
        saveToStorage()
    }

    fun toggleBillPaid(id: String) {
        _bills.value = _bills.value.map {
            if (it.id == id) it.copy(isPaid = !it.isPaid) else it
        }
        saveToStorage()
    }

    fun deleteBill(id: String) {
        _bills.value = _bills.value.filterNot { it.id == id }
        saveToStorage()
    }

    // Calculations
    fun getTotalSpent(): Double {
        return _expenses.value.sumOf { it.amount }
    }

    fun getCategorySpent(category: String): Double {
        return _expenses.value.filter { it.category.equals(category, ignoreCase = true) }.sumOf { it.amount }
    }

    fun getBudgetStatuses(): List<BudgetStatus> {
        val currentBudgets = _budgets.value
        val spentMap = _expenses.value.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        return currentBudgets.map { budget ->
            val spent = spentMap[budget.category] ?: 0.0
            val percent = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
            BudgetStatus(
                category = budget.category,
                spent = spent,
                limit = budget.monthlyLimit,
                percentUsed = percent,
                isOverBudget = spent > budget.monthlyLimit
            )
        }
    }

    fun resetToDefaults() {
        _expenses.value = createSampleExpenses()
        _budgets.value = createSampleBudgets()
        _bills.value = createSampleBills()
        saveToStorage()
    }

    private fun saveToStorage() {
        val sp = prefs ?: return
        try {
            // Save Expenses
            val expArray = JSONArray()
            for (e in _expenses.value) {
                val obj = JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("amount", e.amount)
                    put("currencyCode", e.currencyCode)
                    put("category", e.category)
                    put("timestampMillis", e.timestampMillis)
                    put("notes", e.notes)
                }
                expArray.put(obj)
            }
            // Save Budgets
            val budArray = JSONArray()
            for (b in _budgets.value) {
                val obj = JSONObject().apply {
                    put("category", b.category)
                    put("monthlyLimit", b.monthlyLimit)
                    put("currencyCode", b.currencyCode)
                }
                budArray.put(obj)
            }
            // Save Bills
            val billArray = JSONArray()
            for (bi in _bills.value) {
                val obj = JSONObject().apply {
                    put("id", bi.id)
                    put("title", bi.title)
                    put("amount", bi.amount)
                    put("currencyCode", bi.currencyCode)
                    put("dueEpochMillis", bi.dueEpochMillis)
                    put("isRecurringMonthly", bi.isRecurringMonthly)
                    put("isPaid", bi.isPaid)
                    put("category", bi.category)
                }
                billArray.put(obj)
            }

            sp.edit()
                .putString("saved_expenses", expArray.toString())
                .putString("saved_budgets", budArray.toString())
                .putString("saved_bills", billArray.toString())
                .apply()
        } catch (_: Exception) {
            // Storage safety
        }
    }

    private fun loadFromStorage() {
        val sp = prefs
        if (sp == null || !sp.contains("saved_expenses")) {
            _expenses.value = createSampleExpenses()
            _budgets.value = createSampleBudgets()
            _bills.value = createSampleBills()
            return
        }

        try {
            // Load Expenses
            val expRaw = sp.getString("saved_expenses", "[]") ?: "[]"
            val expArray = JSONArray(expRaw)
            val expList = mutableListOf<OfflineExpense>()
            for (i in 0 until expArray.length()) {
                val obj = expArray.getJSONObject(i)
                expList.add(
                    OfflineExpense(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        currencyCode = obj.optString("currencyCode", "USD"),
                        category = obj.optString("category", "General"),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            _expenses.value = if (expList.isNotEmpty()) expList else createSampleExpenses()

            // Load Budgets
            val budRaw = sp.getString("saved_budgets", "[]") ?: "[]"
            val budArray = JSONArray(budRaw)
            val budList = mutableListOf<CategoryBudget>()
            for (i in 0 until budArray.length()) {
                val obj = budArray.getJSONObject(i)
                budList.add(
                    CategoryBudget(
                        category = obj.getString("category"),
                        monthlyLimit = obj.getDouble("monthlyLimit"),
                        currencyCode = obj.optString("currencyCode", "USD")
                    )
                )
            }
            _budgets.value = if (budList.isNotEmpty()) budList else createSampleBudgets()

            // Load Bills
            val billRaw = sp.getString("saved_bills", "[]") ?: "[]"
            val billArray = JSONArray(billRaw)
            val billList = mutableListOf<BillReminder>()
            for (i in 0 until billArray.length()) {
                val obj = billArray.getJSONObject(i)
                billList.add(
                    BillReminder(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        currencyCode = obj.optString("currencyCode", "USD"),
                        dueEpochMillis = obj.getLong("dueEpochMillis"),
                        isRecurringMonthly = obj.optBoolean("isRecurringMonthly", true),
                        isPaid = obj.optBoolean("isPaid", false),
                        category = obj.optString("category", "Utilities")
                    )
                )
            }
            _bills.value = if (billList.isNotEmpty()) billList else createSampleBills()
        } catch (_: Exception) {
            _expenses.value = createSampleExpenses()
            _budgets.value = createSampleBudgets()
            _bills.value = createSampleBills()
        }
    }
}
