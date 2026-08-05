package com.mother.app.data.repository

/**
 * Thrown when a write violates a business rule (DATABASE_SCHEMA.md: Validasi).
 * Carries a stable [code] so the UI can map it to a localized message without
 * relying on the exception text.
 */
class ValidationException(val code: Code, message: String) : IllegalArgumentException(message) {

    enum class Code {
        BLANK_TITLE,
        BLANK_CATEGORY_NAME,
        DUPLICATE_CATEGORY_NAME,
        CATEGORY_IN_USE,
        END_BEFORE_START,
        NON_POSITIVE_TARGET,
        NON_POSITIVE_DURATION,
        NO_RESTORES_LEFT
    }
}
