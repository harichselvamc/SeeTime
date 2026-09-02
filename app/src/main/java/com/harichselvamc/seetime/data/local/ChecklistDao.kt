package com.harichselvamc.seetime.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM travel_checklists ORDER BY isChecked ASC, sortOrder ASC, id ASC")
    suspend fun getAllItems(): List<TravelChecklistEntity>

    @Query("SELECT * FROM travel_checklists ORDER BY isChecked ASC, sortOrder ASC, id ASC")
    fun getAllItemsFlow(): Flow<List<TravelChecklistEntity>>

    @Query("SELECT * FROM travel_checklists WHERE tripTitle = :tripTitle ORDER BY isChecked ASC, sortOrder ASC, id ASC")
    suspend fun getItemsByTrip(tripTitle: String): List<TravelChecklistEntity>

    @Query("SELECT * FROM travel_checklists WHERE tripTitle = :tripTitle ORDER BY isChecked ASC, sortOrder ASC, id ASC")
    fun getItemsByTripFlow(tripTitle: String): Flow<List<TravelChecklistEntity>>

    @Query("SELECT DISTINCT tripTitle FROM travel_checklists")
    suspend fun getAllTripTitles(): List<String>

    @Query("SELECT COUNT(*) FROM travel_checklists")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TravelChecklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TravelChecklistEntity>)

    @Update
    suspend fun updateItem(item: TravelChecklistEntity)

    @Delete
    suspend fun deleteItem(item: TravelChecklistEntity)

    @Query("DELETE FROM travel_checklists WHERE tripTitle = :tripTitle")
    suspend fun deleteTrip(tripTitle: String)
}
