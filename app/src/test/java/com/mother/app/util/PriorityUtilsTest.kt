package com.mother.app.util

import com.mother.app.data.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [PriorityUtils]. */
class PriorityUtilsTest {

    @Test
    fun `null deadline returns AMAN`() {
        assertEquals(Priority.AMAN, PriorityUtils.calculatePriority(null))
    }

    @Test
    fun `deadline more than 7 days ahead returns AMAN`() {
        val now = 1000000000000L
        val eightDaysLater = now + 8 * 24 * 60 * 60 * 1000L
        assertEquals(Priority.AMAN, PriorityUtils.calculatePriority(eightDaysLater, now))
    }

    @Test
    fun `deadline between 5 and 7 days ahead returns WASPADA`() {
        val now = 1000000000000L
        val sixDaysLater = now + 6 * 24 * 60 * 60 * 1000L
        assertEquals(Priority.WASPADA, PriorityUtils.calculatePriority(sixDaysLater, now))
    }

    @Test
    fun `deadline between 24 hours and 5 days ahead returns MEPET`() {
        val now = 1000000000000L
        val twoDaysLater = now + 2 * 24 * 60 * 60 * 1000L
        val thirtyHoursLater = now + 30 * 60 * 60 * 1000L
        assertEquals(Priority.MEPET, PriorityUtils.calculatePriority(twoDaysLater, now))
        assertEquals(Priority.MEPET, PriorityUtils.calculatePriority(thirtyHoursLater, now))
    }

    @Test
    fun `deadline less than 24 hours ahead returns URGENT`() {
        val now = 1000000000000L
        val twelveHoursLater = now + 12 * 60 * 60 * 1000L
        val oneHourLater = now + 1 * 60 * 60 * 1000L
        assertEquals(Priority.URGENT, PriorityUtils.calculatePriority(twelveHoursLater, now))
        assertEquals(Priority.URGENT, PriorityUtils.calculatePriority(oneHourLater, now))
    }

    @Test
    fun `overdue deadline returns URGENT`() {
        val now = 1000000000000L
        val past = now - 2 * 60 * 60 * 1000L
        assertEquals(Priority.URGENT, PriorityUtils.calculatePriority(past, now))
    }
}
