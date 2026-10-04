package com.sergiodev.bingo.data.local

import androidx.room.TypeConverter

/** Serializes a `List<Int>` as a comma-joined string column, and back. */
class IntListConverter {

    @TypeConverter
    fun fromList(numbers: List<Int>): String = numbers.joinToString(separator = ",")

    @TypeConverter
    fun toList(serialized: String): List<Int> =
        if (serialized.isEmpty()) emptyList() else serialized.split(",").map { it.toInt() }
}
