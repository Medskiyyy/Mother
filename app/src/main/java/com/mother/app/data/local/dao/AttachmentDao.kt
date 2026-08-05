package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.ScheduleAttachmentEntity
import com.mother.app.data.local.entity.TaskAttachmentEntity
import kotlinx.coroutines.flow.Flow

/** DAOs for the per-owner attachment tables (DATABASE_SCHEMA.md part 3). */

@Dao
interface TaskAttachmentDao {

    @Query("SELECT * FROM task_attachment WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun observeForTask(taskId: String): Flow<List<TaskAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: TaskAttachmentEntity)

    @Query("DELETE FROM task_attachment WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ScheduleAttachmentDao {

    @Query("SELECT * FROM schedule_attachment WHERE scheduleId = :scheduleId ORDER BY createdAt ASC")
    fun observeForSchedule(scheduleId: String): Flow<List<ScheduleAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: ScheduleAttachmentEntity)

    @Query("DELETE FROM schedule_attachment WHERE id = :id")
    suspend fun deleteById(id: String)
}
