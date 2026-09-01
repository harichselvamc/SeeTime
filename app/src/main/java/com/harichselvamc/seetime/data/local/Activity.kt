
package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val label: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val category: String = "Uncategorized" // Default category
)
