package com.thefelineco.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val longDate = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())
private val shortDate = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
private val dateTime = DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", Locale.getDefault())

/** "Saturday 3 October 2026". */
fun LocalDate.formatLong(): String = format(longDate)

/** "Sat 3 Oct". */
fun LocalDate.formatShort(): String = format(shortDate)

/** Epoch millis → "3 Oct 2026 · 2:15 PM". */
fun Long.formatDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(dateTime)

/** "Listed today", "Listed yesterday", "Listed 5 days ago". */
fun Long.listedAgo(today: LocalDate = LocalDate.now()): String {
    val listed = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (val days = ChronoUnit.DAYS.between(listed, today)) {
        0L -> "Listed today"
        1L -> "Listed yesterday"
        else -> "Listed $days days ago"
    }
}

/** "+250" / "-150". Positive numbers get an explicit plus sign. */
fun Int.signed(): String = if (this > 0) "+$this" else toString()

/** Adds [item] to the set if it's missing, otherwise removes it. Used by filter chips. */
fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
