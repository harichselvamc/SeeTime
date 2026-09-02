package com.harichselvamc.seetime.data

import android.content.Context
import com.harichselvamc.seetime.data.local.ChecklistDao
import com.harichselvamc.seetime.data.local.TravelChecklistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelChecklistRepository private constructor(
    private val dao: ChecklistDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _itemsState = MutableStateFlow<List<TravelChecklistEntity>>(emptyList())
    val itemsState: StateFlow<List<TravelChecklistEntity>> = _itemsState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: TravelChecklistRepository? = null

        fun getInstance(context: Context): TravelChecklistRepository {
            return INSTANCE ?: synchronized(this) {
                val db = TimeRepository.getInstance(context).database
                INSTANCE ?: TravelChecklistRepository(db.checklistDao()).also {
                    INSTANCE = it
                    it.initialize()
                }
            }
        }

        fun createForTesting(dao: ChecklistDao, scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)): TravelChecklistRepository {
            return TravelChecklistRepository(dao, scope)
        }

        fun getAdapterGuidelineForTimezone(zoneId: String): String {
            return when {
                zoneId.contains("Tokyo", ignoreCase = true) || zoneId.contains("Japan", ignoreCase = true) ->
                    "Japan: Type A & B Plugs (100V, 50/60Hz). Two flat parallel pins."
                zoneId.contains("London", ignoreCase = true) || zoneId.contains("GMT", ignoreCase = true) ->
                    "UK & Ireland: Type G Plugs (230V, 50Hz). Three rectangular pins with safety fuse."
                zoneId.contains("Paris", ignoreCase = true) || zoneId.contains("Berlin", ignoreCase = true) || zoneId.contains("Rome", ignoreCase = true) ->
                    "Eurozone: Type C & F Plugs (230V, 50Hz). Two round pins (Europlug / Schuko)."
                zoneId.contains("New_York", ignoreCase = true) || zoneId.contains("Los_Angeles", ignoreCase = true) ->
                    "USA & Americas: Type A & B Plugs (120V, 60Hz). Two flat pins with grounding pin."
                zoneId.contains("Sydney", ignoreCase = true) || zoneId.contains("Melbourne", ignoreCase = true) ->
                    "Australia & NZ: Type I Plugs (230V, 50Hz). Two V-shaped flat pins with ground."
                zoneId.contains("Kolkata", ignoreCase = true) || zoneId.contains("India", ignoreCase = true) ->
                    "India: Type C, D, & M Plugs (230V, 50Hz). Three round pins in triangular pattern."
                zoneId.contains("Singapore", ignoreCase = true) || zoneId.contains("Hong_Kong", ignoreCase = true) ->
                    "Singapore & Hong Kong: Type G Plugs (230V, 50Hz). Standard British three-pin."
                else ->
                    "International: Universal Multi-Region travel adapter recommended (100V-240V dual voltage)."
            }
        }

        fun generateDefaultTemplate(tripTitle: String, targetTimezone: String): List<TravelChecklistEntity> {
            val adapterInfo = getAdapterGuidelineForTimezone(targetTimezone)

            return listOf(
                // Electronics & Adapters
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Electronics & Power",
                    title = "Destination Plug Adapter",
                    notes = adapterInfo,
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 1
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Electronics & Power",
                    title = "Power Bank (<100Wh for Carry-On)",
                    notes = "Must remain in carry-on luggage per aviation regulations",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 2
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Electronics & Power",
                    title = "USB-C Multi-Charging Cables",
                    notes = "Fast-charge cable for phone, laptop, and noise-canceling headphones",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 3
                ),

                // Documents & Offline Backups
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Documents & Offline Backups",
                    title = "Passport & Offline Digital Scans",
                    notes = "Stored encrypted in SeeTime secure backup or device vault",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 4
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Documents & Offline Backups",
                    title = "Flight Boarding Pass & Hotel Booking (Offline PDF)",
                    notes = "Saved locally before airplane departure",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 5
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Documents & Offline Backups",
                    title = "Offline Travel Maps (Downloaded)",
                    notes = "City map areas downloaded in mapping app for zero-roaming navigation",
                    isChecked = false,
                    isEssential = false,
                    sortOrder = 6
                ),

                // Circadian & Wellness
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Circadian & Health",
                    title = "Melatonin & Sleep Mask",
                    notes = "Follow SeeTime circadian pre-adaptation light exposure schedule",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 7
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Circadian & Health",
                    title = "Noise-Canceling Earplugs",
                    notes = "Silicone pressure-relieving earplugs for flight ascent/descent",
                    isChecked = false,
                    isEssential = false,
                    sortOrder = 8
                ),
                TravelChecklistEntity(
                    tripTitle = tripTitle,
                    targetTimezone = targetTimezone,
                    category = "Circadian & Health",
                    title = "Prescription Medications in Original Bottles",
                    notes = "Packed in carry-on with doctor note if needed",
                    isChecked = false,
                    isEssential = true,
                    sortOrder = 9
                )
            )
        }
    }

    fun initialize() {
        scope.launch {
            seedDefaultTripIfEmpty()
            refreshItems()
        }
    }

    suspend fun seedDefaultTripIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getItemCount() == 0) {
            val defaultItems = generateDefaultTemplate("International Journey", "Asia/Tokyo")
            dao.insertItems(defaultItems)
        }
    }

    suspend fun refreshItems() = withContext(Dispatchers.IO) {
        val items = dao.getAllItems()
        _itemsState.value = items
    }

    fun getItemsFlow(): Flow<List<TravelChecklistEntity>> = dao.getAllItemsFlow()

    fun getItemsByTripFlow(tripTitle: String): Flow<List<TravelChecklistEntity>> =
        dao.getItemsByTripFlow(tripTitle)

    suspend fun getTrips(): List<String> = withContext(Dispatchers.IO) {
        val list = dao.getAllTripTitles()
        if (list.isEmpty()) listOf("International Journey") else list
    }

    suspend fun createTrip(tripTitle: String, targetTimezone: String): List<TravelChecklistEntity> = withContext(Dispatchers.IO) {
        val items = generateDefaultTemplate(tripTitle, targetTimezone)
        dao.insertItems(items)
        refreshItems()
        items
    }

    suspend fun addItem(item: TravelChecklistEntity): Long = withContext(Dispatchers.IO) {
        val id = dao.insertItem(item)
        refreshItems()
        id
    }

    suspend fun toggleItem(item: TravelChecklistEntity) = withContext(Dispatchers.IO) {
        val updated = item.copy(isChecked = !item.isChecked)
        dao.updateItem(updated)
        refreshItems()
    }

    suspend fun deleteItem(item: TravelChecklistEntity) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
        refreshItems()
    }

    suspend fun deleteTrip(tripTitle: String) = withContext(Dispatchers.IO) {
        dao.deleteTrip(tripTitle)
        refreshItems()
    }

    fun calculateProgress(items: List<TravelChecklistEntity>): Float {
        if (items.isEmpty()) return 0f
        val checked = items.count { it.isChecked }
        return (checked.toFloat() / items.size).coerceIn(0f, 1f)
    }
}
