package com.harichselvamc.seetime.data

import com.harichselvamc.seetime.data.local.ChecklistDao
import com.harichselvamc.seetime.data.local.TravelChecklistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeChecklistDao : ChecklistDao {
    val items = mutableMapOf<Long, TravelChecklistEntity>()
    private var nextId = 1L

    override suspend fun getAllItems(): List<TravelChecklistEntity> = items.values.toList()

    override fun getAllItemsFlow(): Flow<List<TravelChecklistEntity>> = flowOf(items.values.toList())

    override suspend fun getItemsByTrip(tripTitle: String): List<TravelChecklistEntity> =
        items.values.filter { it.tripTitle == tripTitle }

    override fun getItemsByTripFlow(tripTitle: String): Flow<List<TravelChecklistEntity>> =
        flowOf(items.values.filter { it.tripTitle == tripTitle })

    override suspend fun getAllTripTitles(): List<String> =
        items.values.map { it.tripTitle }.distinct()

    override suspend fun getItemCount(): Int = items.size

    override suspend fun insertItem(item: TravelChecklistEntity): Long {
        val id = if (item.id == 0L) nextId++ else item.id
        val saved = item.copy(id = id)
        items[id] = saved
        return id
    }

    override suspend fun insertItems(itemsList: List<TravelChecklistEntity>) {
        itemsList.forEach { insertItem(it) }
    }

    override suspend fun updateItem(item: TravelChecklistEntity) {
        items[item.id] = item
    }

    override suspend fun deleteItem(item: TravelChecklistEntity) {
        items.remove(item.id)
    }

    override suspend fun deleteTrip(tripTitle: String) {
        val toRemove = items.values.filter { it.tripTitle == tripTitle }.map { it.id }
        toRemove.forEach { items.remove(it) }
    }
}

class TravelChecklistRepositoryTest {

    private lateinit var fakeDao: FakeChecklistDao
    private lateinit var repository: TravelChecklistRepository

    @Before
    fun setup() {
        fakeDao = FakeChecklistDao()
        repository = TravelChecklistRepository.createForTesting(fakeDao)
    }

    @Test
    fun `generateDefaultTemplate produces comprehensive travel checklist with adapter guidelines`() {
        val template = TravelChecklistRepository.generateDefaultTemplate("Tokyo Trip", "Asia/Tokyo")

        assertTrue(template.isNotEmpty())
        val adapterItem = template.find { it.title.contains("Plug Adapter") }
        assertNotNull(adapterItem)
        assertTrue(adapterItem!!.notes.contains("Type A & B"))
        assertTrue(adapterItem.notes.contains("100V"))
    }

    @Test
    fun `getAdapterGuidelineForTimezone returns accurate region plug info`() {
        val tokyo = TravelChecklistRepository.getAdapterGuidelineForTimezone("Asia/Tokyo")
        assertTrue(tokyo.contains("Type A & B"))

        val london = TravelChecklistRepository.getAdapterGuidelineForTimezone("Europe/London")
        assertTrue(london.contains("Type G"))

        val paris = TravelChecklistRepository.getAdapterGuidelineForTimezone("Europe/Paris")
        assertTrue(paris.contains("Type C & F"))

        val sydney = TravelChecklistRepository.getAdapterGuidelineForTimezone("Australia/Sydney")
        assertTrue(sydney.contains("Type I"))
    }

    @Test
    fun `calculateProgress computes accurate fraction`() {
        val list = listOf(
            TravelChecklistEntity(id = 1, title = "Item 1", isChecked = true),
            TravelChecklistEntity(id = 2, title = "Item 2", isChecked = true),
            TravelChecklistEntity(id = 3, title = "Item 3", isChecked = false),
            TravelChecklistEntity(id = 4, title = "Item 4", isChecked = false)
        )
        val progress = repository.calculateProgress(list)
        assertEquals(0.5f, progress, 0.001f)

        assertEquals(0f, repository.calculateProgress(emptyList()), 0.001f)
    }

    @Test
    fun `toggleItem updates checked state in DAO`() = runBlocking {
        val id = repository.addItem(
            TravelChecklistEntity(title = "Passport", isChecked = false)
        )

        val inserted = fakeDao.items[id]
        assertNotNull(inserted)
        assertFalse(inserted!!.isChecked)

        repository.toggleItem(inserted)
        val updated = fakeDao.items[id]
        assertTrue(updated!!.isChecked)
    }

    @Test
    fun `createTrip populates items in DAO`() = runBlocking {
        val items = repository.createTrip("London Vacation", "Europe/London")
        assertTrue(items.isNotEmpty())

        val stored = fakeDao.getItemsByTrip("London Vacation")
        assertEquals(items.size, stored.size)
    }
}
