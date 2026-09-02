package com.harichselvamc.seetime.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFinanceRepositoryTest {

    private lateinit var repository: OfflineFinanceRepository

    @Before
    fun setup() {
        // Instantiate repository with null context (in-memory mode for tests)
        repository = OfflineFinanceRepository(null)
    }

    @Test
    fun `default sample expenses are populated`() {
        val expenses = repository.expenses.value
        assertTrue(expenses.isNotEmpty())
        assertTrue(expenses.any { it.title.contains("Tokyo Metro") })
        assertTrue(expenses.any { it.category == "Food & Dining" })
    }

    @Test
    fun `add expense updates list and total spending`() {
        val initialTotal = repository.getTotalSpent()
        val newExp = OfflineExpense(
            id = "test-exp-1",
            title = "Espresso & Croissant",
            amount = 7.50,
            currencyCode = "USD",
            category = "Food & Dining"
        )
        repository.addExpense(newExp)

        assertEquals(initialTotal + 7.50, repository.getTotalSpent(), 0.001)
        assertTrue(repository.expenses.value.any { it.id == "test-exp-1" })
    }

    @Test
    fun `delete expense removes from list and updates total spending`() {
        val newExp = OfflineExpense(
            id = "test-delete-1",
            title = "Airport Luggage Storage",
            amount = 12.00,
            currencyCode = "USD",
            category = "Transport"
        )
        repository.addExpense(newExp)
        assertTrue(repository.expenses.value.any { it.id == "test-delete-1" })

        val totalBeforeDelete = repository.getTotalSpent()
        repository.deleteExpense("test-delete-1")

        assertFalse(repository.expenses.value.any { it.id == "test-delete-1" })
        assertEquals(totalBeforeDelete - 12.00, repository.getTotalSpent(), 0.001)
    }

    @Test
    fun `setBudget and budget health calculation`() {
        // Set budget for Transport to $50
        repository.setBudget(CategoryBudget(category = "Transport", monthlyLimit = 50.0))

        val statuses = repository.getBudgetStatuses()
        val transportStatus = statuses.find { it.category == "Transport" }

        assertTrue(transportStatus != null)
        assertEquals(50.0, transportStatus!!.limit, 0.001)
        assertEquals(15.0, transportStatus.spent, 0.001) // from sample Tokyo Metro $15
        assertEquals(0.3f, transportStatus.percentUsed, 0.01f)
        assertFalse(transportStatus.isOverBudget)

        // Add expense that exceeds the limit
        repository.addExpense(
            OfflineExpense(
                title = "Shinkansen Bullet Train",
                amount = 130.0,
                category = "Transport"
            )
        )

        val updatedStatuses = repository.getBudgetStatuses()
        val updatedTransport = updatedStatuses.find { it.category == "Transport" }!!
        assertEquals(145.0, updatedTransport.spent, 0.001)
        assertTrue(updatedTransport.isOverBudget)
    }

    @Test
    fun `bill reminders toggle paid and delete`() {
        val bills = repository.bills.value
        assertTrue(bills.isNotEmpty())

        val unpaidBill = bills.find { !it.isPaid }!!
        val billId = unpaidBill.id

        repository.toggleBillPaid(billId)
        val toggledBill = repository.bills.value.find { it.id == billId }!!
        assertTrue(toggledBill.isPaid)

        repository.deleteBill(billId)
        assertFalse(repository.bills.value.any { it.id == billId })
    }
}
