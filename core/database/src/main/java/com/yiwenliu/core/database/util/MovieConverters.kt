package com.yiwenliu.core.database.util

import androidx.room.TypeConverter

class MovieConverters {
    @TypeConverter
    fun fromGenreIds(genreIds: List<Int>): String = genreIds.joinToString(separator = ",")

    @TypeConverter
    fun toGenreIds(value: String): List<Int> = if (value.isBlank()) {
        emptyList()
    } else {
        value.split(",").mapNotNull(String::toIntOrNull)
    }
}
