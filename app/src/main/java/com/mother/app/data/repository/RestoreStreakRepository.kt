package com.mother.app.data.repository

import com.mother.app.data.local.dao.RestoreHistoryDao
import com.mother.app.data.local.entity.RestoreHistoryEntity
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Restore Streak usage (PRD §14): at most 2 restores per calendar month; the
 * allowance resets on the first day of each month and never stacks.
 */
interface RestoreStreakRepository {
    /** Restores remaining in the current month. */
    suspend fun remainingRestores(): Int
    /** Records a restore usage. Throws [ValidationException] when none remain. */
    suspend fun restore(reason: String?)
    /** Full restore history, oldest first. */
    fun observeAll(): Flow<List<RestoreHistoryEntity>>
}

class RestoreStreakRepositoryImpl(
    private val dao: RestoreHistoryDao
) : RestoreStreakRepository {

    override suspend fun remainingRestores(): Int = MAX_PER_MONTH - usedThisMonth()

    override fun observeAll(): Flow<List<RestoreHistoryEntity>> = dao.observeAll()

    override suspend fun restore(reason: String?) {
        if (remainingRestores() <= 0) {
            throw ValidationException(
                ValidationException.Code.NO_RESTORES_LEFT,
                "Sisa restore bulan ini sudah habis."
            )
        }
        dao.insert(
            RestoreHistoryEntity(
                id = UUID.randomUUID().toString(),
                restoreDate = System.currentTimeMillis(),
                reason = reason
            )
        )
    }

    private suspend fun usedThisMonth(): Int {
        val now = System.currentTimeMillis()
        val monthStart = TimeUtils.startOfMonth(now)
        val monthEnd = TimeUtils.startOfNextMonth(now)
        return dao.countInMonth(monthStart, monthEnd)
    }

    private companion object {
        const val MAX_PER_MONTH = 2
    }
}
