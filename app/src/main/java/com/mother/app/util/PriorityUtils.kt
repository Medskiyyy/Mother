package com.mother.app.util

import com.mother.app.data.model.Priority

/**
 * Utility for automatic task priority determination based on remaining time to deadline.
 *
 * Rules:
 * - No deadline: AMAN
 * - > 7 days (> 168 hours): AMAN
 * - > 5 days (120 to 168 hours): WASPADA
 * - > 24 hours (24 to 120 hours): MEPET
 * - <= 24 hours (or overdue): URGENT
 */
object PriorityUtils {

    fun calculatePriority(deadline: Long?, now: Long = System.currentTimeMillis()): Priority {
        if (deadline == null) return Priority.AMAN
        val diffMs = deadline - now
        val diffHours = diffMs / (60 * 60 * 1000.0)

        return when {
            diffHours > 7 * 24.0 -> Priority.AMAN
            diffHours > 5 * 24.0 -> Priority.WASPADA
            diffHours > 24.0 -> Priority.MEPET
            else -> Priority.URGENT
        }
    }
}
