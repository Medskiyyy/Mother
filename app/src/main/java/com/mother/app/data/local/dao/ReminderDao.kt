package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder WHERE ownerType = :ownerType AND ownerId = :ownerId ORDER BY triggerTime ASC")
    fun observeForOwner(ownerType: String, ownerId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder WHERE enabled = 1 AND triggerTime <= :until ORDER BY triggerTime ASC")
    fun observeDue(until: Long): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Query("DELETE FROM reminder WHERE ownerType = :ownerType AND ownerId = :ownerId")
    suspend fun deleteForOwner(ownerType: String, ownerId: String)
}