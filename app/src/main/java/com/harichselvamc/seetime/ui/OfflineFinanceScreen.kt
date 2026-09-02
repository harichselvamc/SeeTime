package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.data.BillReminder
import com.harichselvamc.seetime.data.CategoryBudget
import com.harichselvamc.seetime.data.OfflineExpense
import com.harichselvamc.seetime.data.OfflineFinanceRepository
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineFinanceScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { OfflineFinanceRepository.getInstance(context) }

    val expenses by repository.expenses.collectAsState()
    val budgets by repository.budgets.collectAsState()
    val bills by repository.bills.collectAsState()

    var activeSection by remember { mutableIntStateOf(0) } // 0: Expenses, 1: Budgets, 2: Bills

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    val totalSpent = repository.getTotalSpent()
    val totalBudget = budgets.sumOf { it.monthlyLimit }
    val budgetStatuses = repository.getBudgetStatuses()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Finance & Budgets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SuccessGreen
                            )
                        }
                    }
                    IconButton(onClick = { repository.resetToDefaults() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset sample data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeSection) {
                        0 -> showAddExpenseDialog = true
                        1 -> showSetBudgetDialog = true
                        2 -> showAddBillDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add New"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Financial Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL SPENT THIS MONTH",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "$%.2f", totalSpent),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Budget Limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "$%.2f", totalBudget),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Total Budget Progress
                        val totalProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
                        val animatedProgress by animateFloatAsState(
                            targetValue = totalProgress.coerceIn(0f, 1f),
                            animationSpec = tween(600),
                            label = "total_progress"
                        )
                        val progressColor by animateColorAsState(
                            targetValue = when {
                                totalProgress > 1f -> MaterialTheme.colorScheme.error
                                totalProgress > 0.8f -> Amber60
                                else -> SuccessGreen
                            },
                            label = "progress_color"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(totalProgress * 100).toInt()}% of budget used",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = progressColor
                                )
                                val remaining = totalBudget - totalSpent
                                Text(
                                    text = if (remaining >= 0) {
                                        String.format(Locale.getDefault(), "$%.2f remaining", remaining)
                                    } else {
                                        String.format(Locale.getDefault(), "$%.2f OVER BUDGET", -remaining)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                                )
                            }
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = progressColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Section Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Expenses (${expenses.size})" to Icons.Filled.Receipt,
                        "Budgets (${budgets.size})" to Icons.Filled.PieChart,
                        "Bill Reminders (${bills.size})" to Icons.Filled.Event
                    ).forEachIndexed { index, (label, icon) ->
                        FilterChip(
                            selected = activeSection == index,
                            onClick = { activeSection = index },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // SECTION 0: EXPENSES LIST
            if (activeSection == 0) {
                if (expenses.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Expenses Logged",
                            description = "Tap the '+' button to log your first offline travel expense."
                        )
                    }
                } else {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onDelete = { repository.deleteExpense(expense.id) }
                        )
                    }
                }
            }

            // SECTION 1: BUDGETS LIST
            if (activeSection == 1) {
                if (budgets.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Category Budgets Set",
                            description = "Tap the '+' button to configure a monthly category spending target."
                        )
                    }
                } else {
                    items(budgetStatuses, key = { it.category }) { status ->
                        BudgetStatusCard(
                            status = status,
                            onDelete = { repository.deleteBudget(status.category) }
                        )
                    }
                }
            }

            // SECTION 2: BILL REMINDERS LIST
            if (activeSection == 2) {
                if (bills.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Bill Reminders",
                            description = "Tap the '+' button to add recurring or one-time payment reminders."
                        )
                    }
                } else {
                    items(bills, key = { it.id }) { bill ->
                        BillReminderCard(
                            bill = bill,
                            onTogglePaid = { repository.toggleBillPaid(bill.id) },
                            onDelete = { repository.deleteBill(bill.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Add Expense
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { expense ->
                repository.addExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    // Dialog: Set Budget
    if (showSetBudgetDialog) {
        SetBudgetDialog(
            existingCategories = budgets.map { it.category },
            onDismiss = { showSetBudgetDialog = false },
            onConfirm = { budget ->
                repository.setBudget(budget)
                showSetBudgetDialog = false
            }
        )
    }

    // Dialog: Add Bill
    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { bill ->
                repository.addBill(bill)
                showAddBillDialog = false
            }
        )
    }
}

// ── Composable Components ─────────────────────────────────────────────

@Composable
private fun ExpenseItemCard(
    expense: OfflineExpense,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val dateStr = remember(expense.timestampMillis) { dateFormat.format(Date(expense.timestampMillis)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Surface(
                shape = CircleShape,
                color = getCategoryColor(expense.category).copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = null,
                        tint = getCategoryColor(expense.category),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (expense.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", expense.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetStatusCard(
    status: com.harichselvamc.seetime.data.BudgetStatus,
    onDelete: () -> Unit
) {
    val progress = status.percentUsed.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "budget_progress"
    )
    val progressColor = when {
        status.isOverBudget -> MaterialTheme.colorScheme.error
        status.percentUsed > 0.8f -> Amber60
        else -> SuccessGreen
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getCategoryIcon(status.category),
                        contentDescription = null,
                        tint = getCategoryColor(status.category),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "$%.2f / $%.2f",
                            status.spent,
                            status.limit
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(status.percentUsed * 100).toInt()}% consumed",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                if (status.isOverBudget) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "Over by $%.2f", status.spent - status.limit),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f left", status.limit - status.spent),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun BillReminderCard(
    bill: BillReminder,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = !bill.isPaid && bill.dueEpochMillis < now
    val daysDiff = ((bill.dueEpochMillis - now) / (1000 * 60 * 60 * 24)).toInt()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val dueDateStr = remember(bill.dueEpochMillis) { dateFormat.format(Date(bill.dueEpochMillis)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTogglePaid) {
                Icon(
                    imageVector = if (bill.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = "Toggle Paid",
                    tint = if (bill.isPaid) SuccessGreen else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Due: $dueDateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (bill.isRecurringMonthly) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "· Monthly",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cobalt60
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        bill.isPaid -> SuccessGreen.copy(alpha = 0.15f)
                        isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else -> Amber60.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            bill.isPaid -> "PAID"
                            isOverdue -> "OVERDUE"
                            daysDiff == 0 -> "DUE TODAY"
                            daysDiff == 1 -> "DUE TOMORROW"
                            else -> "IN $daysDiff DAYS"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = when {
                            bill.isPaid -> SuccessGreen
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> Amber60
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Bill",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocalAtm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (OfflineExpense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(OfflineFinanceRepository.DEFAULT_CATEGORIES.first()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Offline Expense", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description / Merchant") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(OfflineFinanceRepository.DEFAULT_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(
                            OfflineExpense(
                                title = title.trim(),
                                amount = amount,
                                category = selectedCategory,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
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
private fun SetBudgetDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (CategoryBudget) -> Unit
) {
    val availableCategories = OfflineFinanceRepository.DEFAULT_CATEGORIES
    var selectedCategory by remember { mutableStateOf(availableCategories.first()) }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Category Budget", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Budget Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onConfirm(
                            CategoryBudget(
                                category = selectedCategory,
                                monthlyLimit = limit
                            )
                        )
                    }
                },
                enabled = (limitText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Set Target")
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
private fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (BillReminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysAheadText by remember { mutableStateOf("7") }
    var isRecurring by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Utilities") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Bill Due Date Reminder", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Service Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysAheadText,
                    onValueChange = { daysAheadText = it },
                    label = { Text("Due in (Days from now)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Monthly",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val days = daysAheadText.toLongOrNull() ?: 7L
                    val dueMillis = Instant.now().plus(days, ChronoUnit.DAYS).toEpochMilli()
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(
                            BillReminder(
                                title = title.trim(),
                                amount = amount,
                                dueEpochMillis = dueMillis,
                                isRecurringMonthly = isRecurring,
                                category = selectedCategory
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ── Helper Category Icons & Colors ────────────────────────────────────

private fun getCategoryIcon(category: String): ImageVector {
    return when {
        category.contains("Food", ignoreCase = true) || category.contains("Dining", ignoreCase = true) -> Icons.Filled.Fastfood
        category.contains("Transport", ignoreCase = true) -> Icons.Filled.Flight
        category.contains("Lodging", ignoreCase = true) || category.contains("Hotel", ignoreCase = true) -> Icons.Filled.Hotel
        category.contains("Work", ignoreCase = true) || category.contains("Tech", ignoreCase = true) -> Icons.Filled.Laptop
        category.contains("Shopping", ignoreCase = true) -> Icons.Filled.ShoppingBag
        category.contains("Utilities", ignoreCase = true) -> Icons.Filled.Notifications
        else -> Icons.Filled.LocalAtm
    }
}

private fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Food", ignoreCase = true) -> Amber60
        category.contains("Transport", ignoreCase = true) -> Cobalt60
        category.contains("Lodging", ignoreCase = true) -> Color(0xFF9C27B0)
        category.contains("Work", ignoreCase = true) -> Color(0xFF009688)
        category.contains("Shopping", ignoreCase = true) -> Color(0xFFE91E63)
        category.contains("Utilities", ignoreCase = true) -> Color(0xFFFF5722)
        else -> SuccessGreen
    }
}
