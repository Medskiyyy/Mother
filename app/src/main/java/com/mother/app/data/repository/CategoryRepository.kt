package com.mother.app.data.repository

import com.mother.app.data.local.dao.CategoryDao
import com.mother.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for categories. */
interface CategoryRepository {
    fun observeAll(): Flow<List<CategoryEntity>>
    suspend fun getById(id: String): CategoryEntity?
    suspend fun upsert(category: CategoryEntity)
    suspend fun deleteById(id: String)
}

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {
    override fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    override suspend fun getById(id: String): CategoryEntity? = dao.getById(id)
    override suspend fun upsert(category: CategoryEntity) = dao.upsert(category)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}