package com.mother.app.data.repository

import com.mother.app.data.local.dao.HabitDao
import com.mother.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for habits.
 * Enforces DATABASE_SCHEMA.md rules: title is required and target must be
 * greater than zero.
 */
interface HabitRepository {
    fun observeActive(): Flow<List<HabitEntity>>
    suspend fun getById(id: String): HabitEntity?
    suspend fun upsert(habit: HabitEntity)
    suspend fun deleteById(id: String)
    suspend fun setArchived(id: String, archived: Boolean)
}

class HabitRepositoryImpl(private val dao: HabitDao) : HabitRepository {
    override fun observeActive(): Flow<List<HabitEntity>> = dao.observeActive()
    override suspend fun getById(id: String): HabitEntity? = dao.getById(id)

    override suspend fun setArchived(id: String, archived: Boolean) =
        dao.updateArchived(id, archived, System.currentTimeMillis())

    override suspend fun upsert(habit: HabitEntity) {
        if (habit.title.isBlank()) {
            throw ValidationException(ValidationException.Code.BLANK_TITLE, "Judul wajib diisi.")
        }
        if (habit.targetMinute <= 0) {
            throw ValidationException(
                ValidationException.Code.NON_POSITIVE_TARGET,
                "Target harus lebih dari nol."
            )
        }
        dao.upsert(habit.copy(title = habit.title.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}
