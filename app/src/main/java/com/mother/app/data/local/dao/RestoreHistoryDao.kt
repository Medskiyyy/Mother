package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.RestoreHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestoreHistoryDao {

    @Query("SELECT COUNT(*) FROM restore_history WHERE restoreDate >= :monthStart AND restoreDate < :monthEnd")
    suspend fun countInMonth(monthStart: Long, monthEnd: Long): Int

    @Query("SELECT * FROM restore_history ORDER BY restoreDate ASC")
    fun observeAll(): Flow<List<RestoreHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RestoreHistoryEntity)
}