package com.thefelineco.data.local

import androidx.room.TypeConverter

/** Room type converters. Enums are handled by Room automatically, so only lists need one. */
class Converters {
    @TypeConverter
    fun fromList(values: List<String>): String = values.joinToString(SEPARATOR)

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        const val SEPARATOR = "|"
    }
}
