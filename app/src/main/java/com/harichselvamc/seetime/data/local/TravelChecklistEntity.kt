package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_checklists")
data class TravelChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripTitle: String = "General Travel",
    val targetTimezone: String = "UTC",
    val category: String = "Essentials",
    val title: String,
    val notes: String = "",
    val isChecked: Boolean = false,
    val isEssential: Boolean = true,
    val sortOrder: Int = 0
)
