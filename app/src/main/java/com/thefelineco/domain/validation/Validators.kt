package com.thefelineco.domain.validation

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Pure validation functions shared by every form. Each returns `null` when the value is valid,
 * or a short, user-facing error message to show under the field.
 */
object Validators {

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Australian-style numbers: 8–12 digits, optional leading +, spaces allowed. */
    private val PHONE = Regex("^\\+?[0-9 ]{8,15}$")

    fun required(value: String, field: String): String? =
        if (value.isBlank()) "$field is required" else null

    fun name(value: String): String? = when {
        value.isBlank() -> "Name is required"
        value.trim().length < 2 -> "Name is too short"
        value.any { it.isDigit() } -> "Name can't contain numbers"
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !EMAIL.matches(value.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun phone(value: String): String? = when {
        value.isBlank() -> "Phone number is required"
        !PHONE.matches(value.trim()) || value.count { it.isDigit() } !in 8..12 ->
            "Enter a valid phone number"
        else -> null
    }

    /** At least 8 characters with a letter and a number. */
    fun password(value: String): String? = when {
        value.isEmpty() -> "Password is required"
        value.length < 8 -> "Use at least 8 characters"
        value.none { it.isLetter() } || value.none { it.isDigit() } ->
            "Include at least one letter and one number"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Please confirm your password"
        password != confirm -> "Passwords don't match"
        else -> null
    }

    /** Australian postcodes are exactly four digits. */
    fun postcode(value: String): String? = when {
        value.isBlank() -> "Postcode is required"
        value.trim().length != 4 || !value.trim().all { it.isDigit() } -> "Postcode must be 4 digits"
        else -> null
    }

    /**
     * A meet & greet date must be picked, from tomorrow up to [maxDaysAhead] days out,
     * and not on a Monday (the shelter is closed).
     */
    fun appointmentDate(date: LocalDate?, today: LocalDate, maxDaysAhead: Long = 30): String? = when {
        date == null -> "Choose a date"
        !date.isAfter(today) -> "Choose a date from tomorrow onwards"
        date.isAfter(today.plusDays(maxDaysAhead)) -> "Bookings open $maxDaysAhead days ahead"
        date.dayOfWeek == DayOfWeek.MONDAY -> "We're closed on Mondays"
        else -> null
    }

    /** Whole number within [range], e.g. age in months or stock. */
    fun intInRange(value: String, field: String, range: IntRange): String? {
        val number = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> "$field is required"
            number == null -> "$field must be a whole number"
            number !in range -> "$field must be between ${range.first} and ${range.last}"
            else -> null
        }
    }

    /** Decimal number within [range], e.g. weight in kg. */
    fun decimalInRange(value: String, field: String, range: ClosedFloatingPointRange<Double>): String? {
        val number = value.trim().toDoubleOrNull()
        return when {
            value.isBlank() -> "$field is required"
            number == null -> "$field must be a number"
            number !in range -> "$field must be between ${range.start} and ${range.endInclusive}"
            else -> null
        }
    }
}
