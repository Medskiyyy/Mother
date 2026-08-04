package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklist WHERE taskId = :taskId ORDER BY orderIndex ASC")
    fun observeForTask(taskId: String): Flow<List<ChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChecklistEntity)

    @Query("DELETE FROM checklist WHERE id = :id")
    suspend fun deleteById(id: String)
}