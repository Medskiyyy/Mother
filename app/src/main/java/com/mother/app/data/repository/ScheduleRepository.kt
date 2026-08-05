package com.mother.app.data.repository

import com.mother.app.data.local.dao.ScheduleDao
import com.mother.app.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for schedules.
 * Enforces DATABASE_SCHEMA.md rules: title is required and end time must be
 * after start time.
 */
interface ScheduleRepository {
    fun observeAll(): Flow<List<ScheduleEntity>>
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<ScheduleEntity>>
    fun observeRange(from: Long, to: Long): Flow<List<ScheduleEntity>>
    suspend fun getById(id: String): ScheduleEntity?
    suspend fun upsert(schedule: ScheduleEntity)
    suspend fun deleteById(id: String)

    /** True when another schedule overlaps [start, end) (PRD §12 conflict warning). */
    suspend fun hasConflict(start: Long, end: Long, excludeId: String = ""): Boolean

    /** Schedules whose status may still change automatically. */
    suspend fun getPending(): List<ScheduleEntity>

    /** Sets [status] directly (used by the status syncer; skips validation). */
    suspend fun updateStatus(id: String, status: com.mother.app.data.model.StatusSchedule, updatedAt: Long)
}

class ScheduleRepositoryImpl(private val dao: ScheduleDao) : ScheduleRepository {
    override fun observeAll(): Flow<List<ScheduleEntity>> = dao.observeAll()

    override fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<ScheduleEntity>> =
        dao.observeForDay(dayStart, dayEnd)

    override fun observeRange(from: Long, to: Long): Flow<List<ScheduleEntity>> =
        dao.observeRange(from, to)

    override suspend fun getById(id: String): ScheduleEntity? = dao.getById(id)

    override suspend fun upsert(schedule: ScheduleEntity) {
        if (schedule.title.isBlank()) {
            throw ValidationException(ValidationException.Code.BLANK_TITLE, "Judul wajib diisi.")
        }
        if (schedule.endTime <= schedule.startTime) {
            throw ValidationException(
                ValidationException.Code.END_BEFORE_START,
                "Jam selesai harus lebih besar dari jam mulai."
            )
        }
        dao.upsert(schedule.copy(title = schedule.title.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteById(id: String) = dao.deleteById(id)

    override suspend fun hasConflict(start: Long, end: Long, excludeId: String): Boolean =
        dao.countOverlapping(start, end, excludeId) > 0

    override suspend fun getPending(): List<ScheduleEntity> = dao.getPending()

    override suspend fun updateStatus(id: String, status: com.mother.app.data.model.StatusSchedule, updatedAt: Long) =
        dao.updateStatus(id, status.name, updatedAt)
}