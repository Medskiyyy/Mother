package com.mother.app.data.repository

import com.mother.app.data.local.dao.CategoryDao
import com.mother.app.data.local.dao.HabitDao
import com.mother.app.data.local.dao.ScheduleDao
import com.mother.app.data.local.dao.TaskDao
import com.mother.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for categories.
 * Enforces DATABASE_SCHEMA.md rules: unique name, and no deletion while in use.
 */
interface CategoryRepository {
    fun observeAll(): Flow<List<CategoryEntity>>
    suspend fun getById(id: String): CategoryEntity?
    suspend fun upsert(category: CategoryEntity)
    suspend fun deleteById(id: String)
}

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao,
    private val habitDao: HabitDao
) : CategoryRepository {

    override fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    override suspend fun getById(id: String): CategoryEntity? = dao.getById(id)

    override suspend fun upsert(category: CategoryEntity) {
        val name = category.name.trim()
        if (name.isEmpty()) {
            throw ValidationException(
                ValidationException.Code.BLANK_CATEGORY_NAME,
                "Nama kategori wajib diisi."
            )
        }
        // Unique name (case-insensitive), excluding the row being updated.
        val duplicate = dao.getAll().firstOrNull {
            it.id != category.id && it.name.equals(name, ignoreCase = true)
        }
        if (duplicate != null) {
            throw ValidationException(
                ValidationException.Code.DUPLICATE_CATEGORY_NAME,
                "Nama kategori sudah digunakan."
            )
        }
        dao.upsert(category.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteById(id: String) {
        val inUse = taskDao.countByCategory(id) +
            scheduleDao.countByCategory(id) +
            habitDao.countByCategory(id)
        if (inUse > 0) {
            throw ValidationException(
                ValidationException.Code.CATEGORY_IN_USE,
                "Kategori masih digunakan dan tidak dapat dihapus."
            )
        }
        dao.deleteById(id)
    }
}
