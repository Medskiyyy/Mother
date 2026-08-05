package com.mother.app.data.model

/**
 * Enums shared across the domain and data layers, mirroring DATABASE_SCHEMA.md.
 */

enum class Priority { AMAN, WASPADA, MEPET, URGENT }

enum class StatusTask { ACTIVE, COMPLETED, OVERDUE }

enum class StatusSchedule { UPCOMING, RUNNING, COMPLETED, MISSED, CANCELLED }

enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, CUSTOM }

enum class ReminderType { ONCE, REPEAT }

enum class Theme { LIGHT, DARK, SYSTEM }

enum class AttachmentType { IMAGE, PDF }

/** Origin of a StudySession. */
enum class SessionSource { TIMER, MANUAL, POMODORO }